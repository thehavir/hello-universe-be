package dev.havir.hellouniverse.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path

/**
 * Serves the generated thumbnails at `/media/thumb/<date>.webp` in the dev
 * profile, where nginx is not in front of the app. Production never loads this.
 *
 * It adds one handler and leaves Spring's default one alone. Changing
 * `spring.mvc.static-path-pattern` instead would make unknown paths fail with a
 * different error (and break the 404 tests, which run with the dev profile).
 */
@Configuration
@Profile("dev")
class DevMediaConfiguration(
    @Value($$"${media.thumbnail-dir}") thumbnailDir: String,
) : WebMvcConfigurer {
    // Path.toUri() adds the trailing slash only for a folder that exists.
    // Without it, Spring reads the location as a file, not a folder.
    private val location: String =
        Path
            .of(thumbnailDir)
            .toAbsolutePath()
            .toUri()
            .toString()
            .trimEnd('/') +
            "/"

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
            .addResourceHandler("/media/thumb/**")
            .addResourceLocations(location)
    }
}
