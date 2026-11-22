package dev.havir.hellouniverse.configuration

import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.BeforeTest
import kotlin.test.Test

// Only this class, so no controller (and its use case) is needed
@WebMvcTest(DevMediaConfiguration::class)
@ActiveProfiles("dev")
class DevMediaConfigurationTest {
    companion object {
        // One folder for the whole class: Spring reads the property once
        @TempDir
        @JvmStatic
        lateinit var thumbnailDir: Path

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("media.thumbnail-dir") { thumbnailDir.toString() }
        }
    }

    @Autowired
    lateinit var mockMvc: MockMvc

    @BeforeTest
    fun writeThumbnail() {
        Files.write(
            thumbnailDir.resolve("2026-11-22.webp"),
            byteArrayOf(1, 2, 3),
        )
    }

    @Test
    fun `serves a generated thumbnail at media thumb`() {
        mockMvc
            .get("/media/thumb/2026-11-22.webp?v=1700000000")
            .andExpect {
                status { isOk() }
                content { bytes(byteArrayOf(1, 2, 3)) }
            }
    }

    @Test
    fun `returns not found for a thumbnail that does not exist`() {
        mockMvc.get("/media/thumb/1999-01-01.webp").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `does not serve files outside the thumbnail folder`() {
        Files.write(thumbnailDir.resolveSibling("secret.txt"), byteArrayOf(9))

        mockMvc.get("/media/thumb/../secret.txt").andExpect {
            status { isNotFound() }
        }
    }
}
