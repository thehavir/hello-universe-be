package dev.havir.hellouniverse.infrastructure.nasaapi

import org.springframework.boot.context.properties.bind.BindException
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NasaApiPropertiesTest {
    private fun bind(properties: Map<String, String>): NasaApiProperties =
        Binder(MapConfigurationPropertySource(properties))
            .bind("nasa.api", NasaApiProperties::class.java)
            .get()

    @Test
    fun `on bind reads base-url from config`() {
        val result = bind(mapOf("nasa.api.base-url" to "https://abc.com"))

        assertEquals("https://abc.com", result.baseUrl)
    }

    @Test
    fun `on bind fails when base-url is missing`() {
        assertFailsWith<BindException> {
            bind(mapOf("nasa.api.read-timeout" to "500ms"))
        }
    }

    @Test
    fun `on bind reads connect-timeout and read-timeout from config`() {
        val result =
            bind(
                mapOf(
                    "nasa.api.base-url" to "https://abc.com",
                    "nasa.api.connect-timeout" to "2s",
                    "nasa.api.read-timeout" to "500ms",
                ),
            )

        assertEquals(Duration.ofSeconds(2), result.connectTimeout)
        assertEquals(Duration.ofMillis(500), result.readTimeout)
    }

    @Test
    fun `on bind uses default timeouts when they are not configured`() {
        val result = bind(mapOf("nasa.api.base-url" to "https://abc.com"))

        assertEquals(Duration.ofSeconds(5), result.connectTimeout)
        assertEquals(Duration.ofSeconds(15), result.readTimeout)
    }
}
