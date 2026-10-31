package dev.havir.hellouniverse.infrastructure.nasaapi

import dev.havir.hellouniverse.domain.ApodGateway
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.resilience.annotation.EnableResilientMethods
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestClient
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

// @Retryable only works through a Spring proxy, so this test needs a
// (small) Spring context
@SpringJUnitConfig(NasaApodGatewayRetryTest.Config::class)
@TestPropertySource(properties = ["nasa.api.retry.delay=1"])
class NasaApodGatewayRetryTest {
    @Configuration
    @EnableResilientMethods(proxyTargetClass = true)
    class Config {
        private val builder = RestClient.builder()

        @Bean
        fun server(): MockRestServiceServer = MockRestServiceServer.bindTo(builder).build()

        @Bean
        fun gateway(): ApodGateway = NasaApodGateway(builder.build())
    }

    @Autowired
    lateinit var server: MockRestServiceServer

    @Autowired
    lateinit var tested: ApodGateway

    @AfterTest
    fun tearDown() {
        server.reset()
    }

    @Test
    fun `on getApod retries server errors and returns Apod`() {
        server
            .expect(ExpectedCount.times(2), requestTo(APOD_PATH))
            .andRespond(withServerError())
        server
            .expect(requestTo(APOD_PATH))
            .andRespond(withSuccess(APOD_JSON, MediaType.APPLICATION_JSON))

        val result = tested.getApod()

        assertEquals(LocalDate.of(2024, 1, 1), result?.date)
        server.verify()
    }

    @Test
    fun `on getApod throws after max retries on server errors`() {
        server
            .expect(ExpectedCount.times(4), requestTo(APOD_PATH))
            .andRespond(withServerError())

        assertThrows<HttpServerErrorException> { tested.getApod() }
        server.verify()
    }

    @Test
    fun `on getApod does not retry client errors`() {
        server
            .expect(ExpectedCount.once(), requestTo(APOD_PATH))
            .andRespond(withBadRequest())

        assertThrows<HttpClientErrorException> { tested.getApod() }
        server.verify()
    }

    private companion object {
        const val APOD_PATH = "/planetary/apod"
        val APOD_JSON =
            """
            {
                "date": "2024-01-01",
                "title": "Test Title",
                "explanation": "Test explanation",
                "media_type": "image",
                "url": "https://test.com/image",
                "service_version": "v1"
            }
            """.trimIndent()
    }
}
