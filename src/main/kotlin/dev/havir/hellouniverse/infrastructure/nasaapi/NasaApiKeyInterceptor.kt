package dev.havir.hellouniverse.infrastructure.nasaapi

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.client.support.HttpRequestWrapper
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class NasaApiKeyInterceptor(private val apiKey: String) :
    ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        val modifiedUri = UriComponentsBuilder.fromUri(request.uri)
            .queryParam("api_key", apiKey).build().toUri()
        val modifiedRequest = object : HttpRequestWrapper(request) {
            override fun getURI(): URI = modifiedUri
        }

        return execution.execute(modifiedRequest, body)
    }
}
