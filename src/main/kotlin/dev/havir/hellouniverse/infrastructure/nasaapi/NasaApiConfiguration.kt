package dev.havir.hellouniverse.infrastructure.nasaapi

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

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
            .requestInterceptor(nasaApiKeyInterceptor)
            .build()
}
