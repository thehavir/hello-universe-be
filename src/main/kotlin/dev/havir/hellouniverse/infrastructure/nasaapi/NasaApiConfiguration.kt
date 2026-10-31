package dev.havir.hellouniverse.infrastructure.nasaapi

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.net.http.HttpClient

@Configuration
@EnableConfigurationProperties(NasaApiProperties::class)
class NasaApiConfiguration(
    private val nasaApiProperties: NasaApiProperties,
) {
    @Bean
    fun nasaApiInterceptor(): NasaApiKeyInterceptor =
        NasaApiKeyInterceptor(nasaApiProperties.apiKey)

    @Bean
    fun nasaApiRestClient(
        nasaApiKeyInterceptor: NasaApiKeyInterceptor,
    ): RestClient =
        RestClient
            .builder()
            .baseUrl(nasaApiProperties.baseUrl)
            .requestFactory(nasaApiRequestFactory())
            .requestInterceptor(nasaApiKeyInterceptor)
            .build()

    private fun nasaApiRequestFactory(): JdkClientHttpRequestFactory {
        val httpClient =
            HttpClient
                .newBuilder()
                .connectTimeout(nasaApiProperties.connectTimeout)
                .build()
        return JdkClientHttpRequestFactory(httpClient).apply {
            setReadTimeout(nasaApiProperties.readTimeout)
        }
    }
}
