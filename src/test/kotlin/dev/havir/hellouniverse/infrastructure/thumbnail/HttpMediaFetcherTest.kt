package dev.havir.hellouniverse.infrastructure.thumbnail

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.CountDownLatch
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class HttpMediaFetcherTest {
    @TempDir
    lateinit var tempDir: Path
    lateinit var httpClient: HttpClient
    lateinit var tested: HttpMediaFetcher
    lateinit var target: Path
    val url = "https://assets.science.nasa.gov/apod/a.jpg"

    @BeforeEach
    fun setUp() {
        httpClient = mockk()
        tested = HttpMediaFetcher(httpClient)
        target = tempDir.resolve("source")
    }

    private fun givenResponse(
        status: Int,
        body: ByteArray,
    ) {
        val response = mockk<HttpResponse<InputStream>>()
        every { response.statusCode() } returns status
        every { response.body() } returns ByteArrayInputStream(body)
        every {
            httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>())
        } returns response
    }

    /** A body that sends nothing until it is closed, like a stalled server. */
    private fun givenStalledBody() {
        val closed = CountDownLatch(1)
        val stalledBody =
            object : InputStream() {
                override fun read(): Int = read(ByteArray(1), 0, 1)

                override fun read(
                    b: ByteArray,
                    off: Int,
                    len: Int,
                ): Int {
                    closed.await()
                    throw IOException("closed")
                }

                override fun close() = closed.countDown()
            }
        val response = mockk<HttpResponse<InputStream>>()
        every { response.statusCode() } returns 200
        every { response.body() } returns stalledBody
        every {
            httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>())
        } returns response
    }

    @Test
    fun `on download saves the body to the target file`() {
        val body = ByteArray(200_000) { (it % 251).toByte() }
        givenResponse(200, body)

        tested.download(url, target)

        assertContentEquals(body, Files.readAllBytes(target))
    }

    @Test
    fun `on download sends a GET request to the url`() {
        val request = slot<HttpRequest>()
        every {
            httpClient.send(
                capture(request),
                any<HttpResponse.BodyHandler<InputStream>>(),
            )
        } answers {
            mockk<HttpResponse<InputStream>> {
                every { statusCode() } returns 200
                every { body() } returns ByteArrayInputStream(ByteArray(1))
            }
        }

        tested.download(url, target)

        assertEquals(URI(url), request.captured.uri())
        assertEquals("GET", request.captured.method())
    }

    @Test
    fun `on download throws SourceNotFoundException for 404`() {
        givenResponse(404, "not found".toByteArray())

        assertFailsWith<SourceNotFoundException> {
            tested.download(
                url,
                target,
            )
        }
    }

    @Test
    fun `on download throws SourceNotFoundException for 410`() {
        givenResponse(410, "gone".toByteArray())

        assertFailsWith<SourceNotFoundException> {
            tested.download(
                url,
                target,
            )
        }
    }

    @Test
    fun `on download throws ThumbnailException for a server error`() {
        givenResponse(500, "error".toByteArray())

        val error =
            assertFailsWith<ThumbnailException> { tested.download(url, target) }
        assertEquals(ThumbnailException::class, error::class)
    }

    @Test
    fun `on download throws ThumbnailException when the body is larger than 64 MB`() {
        val response = mockk<HttpResponse<InputStream>>()
        every { response.statusCode() } returns 200
        // Endless body: the limit must stop the copy
        every { response.body() } returns
            object : InputStream() {
                override fun read(): Int = 0

                override fun read(
                    b: ByteArray,
                    off: Int,
                    len: Int,
                ): Int = len
            }
        every {
            httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>())
        } returns response

        assertFailsWith<ThumbnailException> { tested.download(url, target) }
    }

    @Test
    fun `on download throws ThumbnailException when the request fails`() {
        every {
            httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>())
        } throws IOException("connection refused")

        assertFailsWith<ThumbnailException> { tested.download(url, target) }
    }

    @Test
    fun `on download refuses http and does not send a request`() {
        assertFailsWith<ThumbnailException> {
            tested.download("http://example.com/a.jpg", target)
        }
        verify(exactly = 0) {
            httpClient.send(any(), any<HttpResponse.BodyHandler<InputStream>>())
        }
    }

    @Test
    fun `on download refuses a file url`() {
        assertFailsWith<ThumbnailException> {
            tested.download("file:///etc/passwd", target)
        }
    }

    @Test
    fun `on readText returns the body as text`() {
        givenResponse(200, """{"thumbnail_url":"x"}""".toByteArray())

        assertEquals(
            """{"thumbnail_url":"x"}""",
            tested.readText("https://vimeo.com/api/oembed.json"),
        )
    }

    @Test
    fun `on readText throws when the body is larger than 64 KB`() {
        givenResponse(200, ByteArray(64 * 1024 + 1))

        assertFailsWith<ThumbnailException> {
            tested.readText("https://vimeo.com/api/oembed.json")
        }
    }

    @Test
    fun `on download throws ThumbnailException when the body stalls longer than the body timeout`() {
        tested = HttpMediaFetcher(httpClient, Duration.ofMillis(300))
        givenStalledBody()
        val start = System.nanoTime()

        val error =
            assertFailsWith<ThumbnailException> { tested.download(url, target) }

        assertTrue { error.message!!.contains("took longer than") }
        assertTrue {
            Duration.ofNanos(System.nanoTime() - start) < Duration.ofSeconds(5)
        }
    }

    @Test
    fun `on readText throws ThumbnailException when the body stalls longer than the body timeout`() {
        tested = HttpMediaFetcher(httpClient, Duration.ofMillis(300))
        givenStalledBody()

        val error =
            assertFailsWith<ThumbnailException> {
                tested.readText("https://vimeo.com/api/oembed.json")
            }

        assertTrue { error.message!!.contains("took longer than") }
    }

    @Test
    fun `on download does not time out a body that is read in time`() {
        tested = HttpMediaFetcher(httpClient, Duration.ofSeconds(5))
        givenResponse(200, ByteArray(1000) { 7 })

        tested.download(url, target)

        assertEquals(1000, Files.size(target))
    }
}
