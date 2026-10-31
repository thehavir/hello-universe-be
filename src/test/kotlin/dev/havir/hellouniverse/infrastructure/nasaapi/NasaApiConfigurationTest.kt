package dev.havir.hellouniverse.infrastructure.nasaapi

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.InetSocketAddress
import java.net.URI
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NasaApiConfigurationTest {
    lateinit var server: HttpServer
    private val release = CountDownLatch(1)
    private var receivedUri: URI? = null

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        // Handler for this URL: Records the request and answers right away
        server.createContext("/planetary/apod") { exchange ->
            receivedUri = exchange.requestURI
            val body = RESPONSE_BODY.toByteArray()
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }
        }
        // Handler for this URL: Accepts requests but never answers until the test ends
        server.createContext("/slow") { exchange ->
            release.await(SERVER_HOLD_SECONDS, TimeUnit.SECONDS)
            exchange.close()
        }
        server.start()
    }

    @AfterEach
    fun tearDown() {
        release.countDown()
        server.stop(0)
    }

    private fun restClient(
        apiKey: String = "key",
        readTimeout: Duration = Duration.ofSeconds(5),
    ): RestClient {
        val configuration =
            NasaApiConfiguration(
                NasaApiProperties(
                    baseUrl = "http://127.0.0.1:${server.address.port}",
                    apiKey = apiKey,
                    readTimeout = readTimeout,
                ),
            )
        return configuration.nasaApiRestClient(
            configuration.nasaApiInterceptor(),
        )
    }

    @Test
    fun `on nasaApiRestClient request is sent to the path under the configured base-url`() {
        restClient()
            .get()
            .uri("/planetary/apod")
            .retrieve()
            .toBodilessEntity()

        assertEquals("/planetary/apod", receivedUri?.path)
    }

    @Test
    fun `on nasaApiRestClient request has the configured api_key query param`() {
        restClient(apiKey = "api-key-xyz-213")
            .get()
            .uri("/planetary/apod")
            .retrieve()
            .toBodilessEntity()

        assertEquals("api_key=api-key-xyz-213", receivedUri?.query)
    }

    @Test
    fun `on nasaApiRestClient request keeps its own query params next to api_key`() {
        restClient(apiKey = "key")
            .get()
            .uri("/planetary/apod?thumbs=true")
            .retrieve()
            .toBodilessEntity()

        assertEquals("thumbs=true&api_key=key", receivedUri?.query)
    }

    @Test
    fun `on nasaApiRestClient request returns the response body`() {
        val result =
            restClient()
                .get()
                .uri("/planetary/apod")
                .retrieve()
                .body<String>()

        assertEquals(RESPONSE_BODY, result)
    }

    @Test
    fun `on nasaApiRestClient request fails with ResourceAccessException when server does not answer within read timeout`() {
        val tested = restClient(readTimeout = Duration.ofMillis(200))

        val start = System.nanoTime()
        assertThrows<ResourceAccessException> {
            tested
                .get()
                .uri("/slow")
                .retrieve()
                .toBodilessEntity()
        }
        val elapsed = Duration.ofNanos(System.nanoTime() - start)

        assertTrue(elapsed < Duration.ofSeconds(2), "took $elapsed")
    }

    companion object {
        private const val SERVER_HOLD_SECONDS = 10L
        private const val RESPONSE_BODY = """{"title":"M83"}"""
    }
}
