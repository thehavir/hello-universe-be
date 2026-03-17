package dev.havir.hellouniverse.infrastructure.nasaapi

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.model.MediaType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNull
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.springframework.http.MediaType as HttpMediaType

class NasaApodGatewayTest {
    lateinit var restClientBuilder: RestClient.Builder
    lateinit var mockServer: MockRestServiceServer
    lateinit var tested: ApodGateway

    @BeforeEach
    fun setUp() {
        restClientBuilder = RestClient.builder()
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build()
    }

    @Test
    fun `on getApod creates a HTTP GET method`() {
        mockServer.expect(method(HttpMethod.GET))
            .andRespond(withStatus(HttpStatus.ACCEPTED))
        tested = NasaApodGateway(restClientBuilder.build())

        tested.getApod()

        mockServer.verify()
    }

    @Test
    fun `on getApod creates uri with correct path`() {
        mockServer.expect(requestTo("/planetary/apod"))
            .andRespond(withStatus(HttpStatus.ACCEPTED))
        tested = NasaApodGateway(restClientBuilder.build())

        tested.getApod()

        mockServer.verify()
    }

    @Test
    fun `on getApod returns null when API returns empty`() {
        mockServer.expect(requestTo("/planetary/apod")).andRespond(
            withSuccess("", HttpMediaType.APPLICATION_JSON)
        )
        tested = NasaApodGateway(restClientBuilder.build())

        val result = tested.getApod()

        assertNull(result)
    }

    @Test
    fun `on getApod returns Apod`() {
        val json = """
    {
        "date": "2024-01-01",
        "title": "Test Title",
        "explanation": "Test explanation",
        "media_type": "image",
        "url": "https://test.com",
        "hdurl": "https://test.com/hd",
        "thumbnail_url": "https://test.com/thumb",
        "service_version": "v1"
    }
"""
        mockServer.expect(requestTo("/planetary/apod")).andRespond(
            withSuccess(
                json, HttpMediaType.APPLICATION_JSON
            )
        )
        tested = NasaApodGateway(restClientBuilder.build())

        val result = tested.getApod()

        assertEquals(
            result, TestModels.apod(
                date = LocalDate.of(2024, 1, 1),
                title = "Test Title",
                explanation = "Test explanation",
                mediaType = MediaType.IMAGE,
                url = "https://test.com",
                hdUrl = "https://test.com/hd",
                thumbnailUrl = "https://test.com/thumb",
                serviceVersion = "v1"
            )
        )
    }

    @Test
    fun `on getApod returns Apod when media_type is image`() {
        val json = """
    {
        "date": "2024-01-01",
        "title": "Test Title",
        "explanation": "Test explanation",
        "media_type": "image",
        "service_version": "v1"
    }
"""
        mockServer.expect(requestTo("/planetary/apod")).andRespond(
            withSuccess(
                json, HttpMediaType.APPLICATION_JSON
            )
        )
        tested = NasaApodGateway(restClientBuilder.build())

        val result = tested.getApod()

        assertEquals(
            result, TestModels.apod(
                date = LocalDate.of(2024, 1, 1),
                title = "Test Title",
                explanation = "Test explanation",
                mediaType = MediaType.IMAGE,
                serviceVersion = "v1"
            )
        )
    }

    @Test
    fun `on getApod returns Apod when media_type is video`() {
        val json = """
    {
        "date": "2024-01-01",
        "title": "Test Title",
        "explanation": "Test explanation",
        "media_type": "video",
        "service_version": "v1"
    }
"""
        mockServer.expect(requestTo("/planetary/apod")).andRespond(
            withSuccess(
                json, HttpMediaType.APPLICATION_JSON
            )
        )
        tested = NasaApodGateway(restClientBuilder.build())

        val result = tested.getApod()

        assertEquals(
            result, TestModels.apod(
                date = LocalDate.of(2024, 1, 1),
                title = "Test Title",
                explanation = "Test explanation",
                mediaType = MediaType.VIDEO,
                serviceVersion = "v1"
            )
        )
    }

    @Test
    fun `on getApod returns Apod when media_type is other`() {
        val json = """
    {
        "date": "2024-01-01",
        "title": "Test Title",
        "explanation": "Test explanation",
        "media_type": "other",
        "service_version": "v1"
    }
"""
        mockServer.expect(requestTo("/planetary/apod")).andRespond(
            withSuccess(
                json, HttpMediaType.APPLICATION_JSON
            )
        )
        tested = NasaApodGateway(restClientBuilder.build())

        val result = tested.getApod()

        assertEquals(
            result, TestModels.apod(
                date = LocalDate.of(2024, 1, 1),
                title = "Test Title",
                explanation = "Test explanation",
                mediaType = MediaType.OTHER,
                serviceVersion = "v1"
            )
        )
    }

    @Test
    fun `on getApod throws when response is not correct`() {
        val jsonWithoutDate = """
    {
        "title": "Test Title",
        "explanation": "Test explanation",
        "media_type": "IMAGE",
        "url": "https://test.com",
        "service_version": "v1"
    }
"""
        mockServer.expect(requestTo("/planetary/apod")).andRespond(
            withSuccess(jsonWithoutDate, HttpMediaType.APPLICATION_JSON)
        )
        tested = NasaApodGateway(restClientBuilder.build())

        assertFailsWith<RestClientException> { tested.getApod() }
    }

    @Test
    fun `on getApod throws when API returns error`() {
        mockServer.expect(requestTo("/planetary/apod")).andRespond(
            withStatus(HttpStatus.GATEWAY_TIMEOUT)
        )
        tested = NasaApodGateway(restClientBuilder.build())

        assertFailsWith<RestClientException> { tested.getApod() }
    }
}
