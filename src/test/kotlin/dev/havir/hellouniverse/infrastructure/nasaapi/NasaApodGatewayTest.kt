package dev.havir.hellouniverse.infrastructure.nasaapi

import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType
import dev.havir.hellouniverse.infrastructure.nasaapi.media.ApodMedia
import dev.havir.hellouniverse.infrastructure.nasaapi.media.ApodMediaExtractor
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.anything
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import org.springframework.http.MediaType as HttpMediaType

class NasaApodGatewayTest {
    private lateinit var nasaApi: MockRestServiceServer
    private val extractor = mockk<ApodMediaExtractor>()
    private lateinit var tested: NasaApodGateway

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder()
        nasaApi = MockRestServiceServer.bindTo(builder).build()
        tested = NasaApodGateway(builder.build(), extractor)
    }

    @Test
    fun `on getApod sends GET with the yyMMdd date code to the APOD Basic API`() {
        nasaApi
            .expect(requestTo(startsWith("/wp-json/wp/v2/apod-basic/261116")))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("", HttpMediaType.APPLICATION_JSON))

        tested.getApod(DATE)

        nasaApi.verify()
    }

    @Test
    fun `on getApod returns null when NASA has not published the APOD yet`() {
        nasaApi
            .expect(anything())
            .andRespond(
                withStatus(HttpStatus.NOT_FOUND)
                    .contentType(HttpMediaType.APPLICATION_JSON)
                    .body(
                        """{"code":"apod_basic_not_found","message":"APOD not found."}""",
                    ),
            )

        val result = tested.getApod(DATE)

        assertNull(result)
    }

    @Test
    fun `on getApod throws when NASA returns 404 for an unknown route`() {
        nasaApi
            .expect(anything())
            .andRespond(
                withStatus(HttpStatus.NOT_FOUND)
                    .contentType(HttpMediaType.APPLICATION_JSON)
                    .body("""{"code":"rest_no_route"}"""),
            )

        assertFailsWith<HttpClientErrorException.NotFound> {
            tested.getApod(DATE)
        }
    }

    @Test
    fun `on getApod throws when NASA returns 404 with a non-JSON body`() {
        nasaApi
            .expect(anything())
            .andRespond(
                withStatus(HttpStatus.NOT_FOUND)
                    .contentType(HttpMediaType.TEXT_HTML)
                    .body("<html>Not Found</html>"),
            )

        assertFailsWith<RestClientException> { tested.getApod(DATE) }
    }

    @Test
    fun `on getApod returns null when API returns empty body`() {
        nasaApi
            .expect(anything())
            .andRespond(withSuccess("", HttpMediaType.APPLICATION_JSON))

        val result = tested.getApod(DATE)

        assertNull(result)
    }

    @Test
    fun `on getApod maps NASA record to Apod`() {
        val json =
            """
            {
                "date": "2026-11-16",
                "post_id": 123,
                "title": "Earth &amp; Moon",
                "permalink": "https://science.nasa.gov/apod/earth-and-moon/",
                "explanation": "The <b>Moon</b><script>bad()</script> <a href='https://moon.com'>here</a>",
                "credit": "<a href='https://jane.com'>Jane</a><script>bad()</script>",
                "copyright": "<b>Jane Doe</b><script>bad()</script>",
                "media_type": "image",
                "basic_html": "<img src='https://apod.nasa.gov/moon.jpg'>",
                "basic_html_url": "https://science.nasa.gov/apod/earth-and-moon/html"
            }
            """.trimIndent()
        nasaApi
            .expect(anything())
            .andRespond(withSuccess(json, HttpMediaType.APPLICATION_JSON))
        every {
            extractor.extract("<img src='https://apod.nasa.gov/moon.jpg'>")
        } returns ApodMedia(MediaType.IMAGE, "https://apod.nasa.gov/moon.jpg")

        val result = tested.getApod(DATE)

        assertEquals(
            Apod(
                date = LocalDate.of(2026, 11, 16),
                title = "Earth & Moon",
                // explanation, credit and copyright: <script> removed,
                // rel="nofollow" added to links
                explanation =
                    "The <b>Moon</b> " +
                        "<a href=\"https://moon.com\" rel=\"nofollow\">here</a>",
                credit =
                    "<a href=\"https://jane.com\" rel=\"nofollow\">Jane</a>",
                copyright = "<b>Jane Doe</b>",
                // media type and url come from the extractor
                mediaType = MediaType.IMAGE,
                url = "https://apod.nasa.gov/moon.jpg",
                thumbnailUrl = null,
                sourceUrl = "https://science.nasa.gov/apod/earth-and-moon/",
            ),
            result,
        )
    }

    @Test
    fun `on getApod returns null credit when NASA credit is null`() {
        val json =
            """
            {
                "date": "2026-11-16",
                "post_id": 123,
                "title": "Moon",
                "permalink": "https://science.nasa.gov/apod/moon/",
                "explanation": "The Moon",
                "credit": null,
                "copyright": "Jane Doe",
                "media_type": "image",
                "basic_html": "<img src='https://apod.nasa.gov/moon.jpg'>",
                "basic_html_url": "https://science.nasa.gov/apod/moon/html"
            }
            """.trimIndent()
        nasaApi
            .expect(anything())
            .andRespond(withSuccess(json, HttpMediaType.APPLICATION_JSON))
        every {
            extractor.extract("<img src='https://apod.nasa.gov/moon.jpg'>")
        } returns ApodMedia(MediaType.IMAGE, "https://apod.nasa.gov/moon.jpg")

        val result = tested.getApod(DATE)

        assertNull(result?.credit)
    }

    @Test
    fun `on getApod returns null copyright when NASA copyright is missing`() {
        // The JSON has no "copyright" field at all
        val json =
            """
            {
                "date": "2026-11-16",
                "post_id": 123,
                "title": "Moon",
                "permalink": "https://science.nasa.gov/apod/moon/",
                "explanation": "The Moon",
                "credit": "Jane Doe",
                "media_type": "image",
                "basic_html": "<img src='https://apod.nasa.gov/moon.jpg'>",
                "basic_html_url": "https://science.nasa.gov/apod/moon/html"
            }
            """.trimIndent()
        nasaApi
            .expect(anything())
            .andRespond(withSuccess(json, HttpMediaType.APPLICATION_JSON))
        every {
            extractor.extract("<img src='https://apod.nasa.gov/moon.jpg'>")
        } returns ApodMedia(MediaType.IMAGE, "https://apod.nasa.gov/moon.jpg")

        val result = tested.getApod(DATE)

        assertNull(result?.copyright)
    }

    @Test
    fun `on getApod throws when extractor finds no media`() {
        val json =
            """
            {
                "date": "2026-11-16",
                "post_id": 123,
                "title": "Moon",
                "permalink": "https://science.nasa.gov/apod/moon/",
                "explanation": "The Moon",
                "media_type": "image",
                "basic_html": "<p>No media here</p>",
                "basic_html_url": "https://science.nasa.gov/apod/moon/html"
            }
            """.trimIndent()
        nasaApi
            .expect(anything())
            .andRespond(withSuccess(json, HttpMediaType.APPLICATION_JSON))
        every { extractor.extract("<p>No media here</p>") } returns null

        assertFailsWith<IllegalStateException> { tested.getApod(DATE) }
    }

    @Test
    fun `on getApod throws when response has no date`() {
        val jsonWithoutDate =
            """
            {
                "post_id": 123,
                "title": "Moon",
                "permalink": "https://science.nasa.gov/apod/moon/",
                "explanation": "The Moon",
                "media_type": "image",
                "basic_html": "<img src='https://apod.nasa.gov/moon.jpg'>",
                "basic_html_url": "https://science.nasa.gov/apod/moon/html"
            }
            """.trimIndent()
        nasaApi
            .expect(anything())
            .andRespond(
                withSuccess(jsonWithoutDate, HttpMediaType.APPLICATION_JSON),
            )

        assertFailsWith<RestClientException> { tested.getApod(DATE) }
    }

    @Test
    fun `on getApod throws when API returns error`() {
        nasaApi
            .expect(anything())
            .andRespond(withStatus(HttpStatus.GATEWAY_TIMEOUT))

        assertFailsWith<RestClientException> { tested.getApod(DATE) }
    }

    private companion object {
        val DATE: LocalDate = LocalDate.of(2026, 11, 16)
    }
}
