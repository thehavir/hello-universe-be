package dev.havir.hellouniverse.infrastructure.nasaapi

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "nasa.api")
data class NasaApiProperties(
    val baseUrl: String,
    val apiKey: String,
    val connectTimeout: Duration = DEFAULT_CONNECT_TIMEOUT,
    val readTimeout: Duration = DEFAULT_READ_TIMEOUT,
) {
    companion object {
        private val DEFAULT_CONNECT_TIMEOUT: Duration = Duration.ofSeconds(5)
        private val DEFAULT_READ_TIMEOUT: Duration = Duration.ofSeconds(15)
    }
}
