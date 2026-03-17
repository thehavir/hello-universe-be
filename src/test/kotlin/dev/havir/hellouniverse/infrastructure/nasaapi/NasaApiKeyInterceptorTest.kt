package dev.havir.hellouniverse.infrastructure.nasaapi

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpResponse
import java.net.URI
import kotlin.test.Test
import kotlin.test.assertEquals


class NasaApiKeyInterceptorTest {
    lateinit var httpRequest: HttpRequest
    lateinit var execution: ClientHttpRequestExecution

    @BeforeEach
    fun setUp() {
        httpRequest = mockk()
        execution = mockk()
    }

    @Test
    fun `on intercept executes ClientHttpRequestExecution with the appended api_key query param to the URL`() {
        val expectedResult = "api-key-xyz-213!"
        val uri = URI.create("https://abc.com")
        val tested = NasaApiKeyInterceptor(expectedResult)
        val slot = slot<HttpRequest>()
        every {
            execution.execute(
                capture(slot), any()
            )
        } returns MockClientHttpResponse()
        every { httpRequest.uri } returns uri

        tested.intercept(
            request = httpRequest, body = byteArrayOf(), execution = execution
        )

        assertEquals("api_key=${expectedResult}", slot.captured.uri.query)
    }

    @Test
    fun `intercept does not change URL itself`() {
        val expectedResult = URI.create("https://localhost")
        val slot = slot<HttpRequest>()
        val tested = NasaApiKeyInterceptor("key")
        every {
            execution.execute(
                capture(slot), any()
            )
        } returns MockClientHttpResponse()
        every { httpRequest.uri } returns expectedResult

        tested.intercept(
            request = httpRequest, body = byteArrayOf(), execution = execution
        )

        assertEquals(
            URI.create("https://localhost?api_key=key"), slot.captured.uri
        )
    }
}
