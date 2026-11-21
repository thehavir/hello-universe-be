package dev.havir.hellouniverse.infrastructure.thumbnail

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.model.MediaType
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import tools.jackson.databind.json.JsonMapper
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Runs the real vips tools on pictures made by vips itself, with no network.
 * Skipped when vips is not installed (`brew install vips` / `libvips-tools`).
 */
class VipsThumbnailGeneratorToolsTest {
    @TempDir
    lateinit var tempDir: Path
    val runner = ProcessCommandRunner()
    val timeout: Duration = Duration.ofSeconds(30)

    /** Answers every download with the picture at [picture]. */
    class LocalFetcher(
        var picture: Path,
    ) : MediaFetcher {
        override fun download(
            url: String,
            target: Path,
        ) {
            Files.copy(picture, target)
        }

        override fun readText(url: String): String = error("not used")
    }

    @BeforeEach
    fun requireVips() {
        val installed =
            runCatching { runner.run(listOf("vips", "--version"), timeout).succeeded }
                .getOrDefault(false)
        assumeTrue(installed, "vips is not installed")
    }

    private fun vips(vararg args: String) {
        val result = runner.run(listOf("vips") + args, timeout)
        check(result.succeeded) { result.output }
    }

    private fun size(file: Path): Pair<Int, Int> {
        fun field(name: String) =
            runner
                .run(listOf("vipsheader", "-f", name, file.toString()), timeout)
                .output
                .trim()
                .toInt()
        return field("width") to field("height")
    }

    /** A plain grey picture of the given size. */
    private fun greyPicture(
        name: String,
        width: Int,
        height: Int,
    ): Path {
        val black = tempDir.resolve("$name-black.png")
        val grey = tempDir.resolve("$name.jpg")
        vips("black", black.toString(), width.toString(), height.toString(), "--bands", "3")
        vips("linear", black.toString(), grey.toString(), "1", "128")
        return grey
    }

    private fun generate(
        fetcher: LocalFetcher,
        mediaType: MediaType,
        url: String,
    ): Path {
        val thumbnailDir = tempDir.resolve("thumb")
        val tested =
            VipsThumbnailGenerator(
                ThumbnailSourceResolver(),
                fetcher,
                runner,
                JsonMapper.builder().build(),
                thumbnailDir.toString(),
            )
        tested.generate(TestModels.apod(date = LocalDate.of(2026, 11, 21), mediaType = mediaType, url = url))
        return thumbnailDir.resolve("2026-11-21.webp")
    }

    @Test
    fun `makes a 480 wide webp from a large landscape picture`() {
        val fetcher = LocalFetcher(greyPicture("wide", 2000, 1000))

        val thumbnail = generate(fetcher, MediaType.IMAGE, "https://x.test/a.jpg")

        assertEquals(480 to 240, size(thumbnail))
        assertEquals("RIFF", String(Files.readAllBytes(thumbnail).copyOfRange(0, 4)))
    }

    @Test
    fun `limits a tall picture to 960 high`() {
        val fetcher = LocalFetcher(greyPicture("tall", 1000, 5000))

        val thumbnail = generate(fetcher, MediaType.IMAGE, "https://x.test/a.jpg")

        assertEquals(192 to 960, size(thumbnail))
    }

    @Test
    fun `does not enlarge a small picture`() {
        val fetcher = LocalFetcher(greyPicture("small", 320, 180))

        val thumbnail = generate(fetcher, MediaType.IMAGE, "https://x.test/a.jpg")

        assertEquals(320 to 180, size(thumbnail))
    }
}
