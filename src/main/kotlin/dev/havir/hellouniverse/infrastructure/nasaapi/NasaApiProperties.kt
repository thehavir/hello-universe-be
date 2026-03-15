package dev.havir.hellouniverse.infrastructure.nasaapi

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "nasa.api")
data class NasaApiProperties(val baseUrl: String, val apiKey: String)
