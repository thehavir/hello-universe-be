package dev.havir.hellouniverse.infrastructure.thumbnail

import dev.havir.hellouniverse.TestModels
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import tools.jackson.databind.json.JsonMapper
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.listDirectoryEntries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VipsThumbnailGeneratorTest {
    @TempDir
    lateinit var tempDir: Path
    lateinit var thumbnailDir: Path
    lateinit var sourceResolver: ThumbnailSourceResolver
    lateinit var mediaFetcher: MediaFetcher
    lateinit var commandRunner: CommandRunner
    lateinit var tested: VipsThumbnailGenerator
    val date: LocalDate = LocalDate.of(2026, 11, 21)
    val apod = TestModels.apod(date = date)
    val imageUrl = "https://assets.science.nasa.gov/apod/a.jpg"
    val videoUrl = "https://assets.science.nasa.gov/apod/a.mp4"

    /** The path the source was downloaded to (inside the work folder). */
    val downloadTarget = slot<Path>()

    @BeforeEach
    fun setUp() {
        thumbnailDir = tempDir.resolve("media/thumb")
        sourceResolver = mockk()
        mediaFetcher = mockk()
        commandRunner = mockk()
        tested =
            VipsThumbnailGenerator(
                sourceResolver,
                mediaFetcher,
                commandRunner,
                JsonMapper.builder().build(),
                thumbnailDir.toString(),
            )
    }

    private fun givenSource(source: ThumbnailSource) {
        every { sourceResolver.resolve(apod) } returns source
    }

    private fun givenDownloadWorks() {
        every { mediaFetcher.download(any(), capture(downloadTarget)) } answers {
            Files.writeString(downloadTarget.captured, "picture")
        }
    }

    /** vipsthumbnail writes the file named before the "[...]" options. */
    private fun givenVipsthumbnailWrites(content: String = "webp") {
        every { commandRunner.run(match { it.first() == "vipsthumbnail" }, any()) } answers {
            val output = firstArg<List<String>>().last().substringBefore("[")
            Files.writeString(Path.of(output), content)
            CommandResult(0, "")
        }
    }

    /** ffmpeg writes a frame only for these seconds. */
    private fun givenFfmpegFramesAt(vararg seconds: Int) {
        every { commandRunner.run(match { it.first() == "ffmpeg" }, any()) } answers {
            val command = firstArg<List<String>>()
            val second = command[command.indexOf("-ss") + 1].toInt()
            if (second in seconds) Files.writeString(Path.of(command.last()), "jpg")
            CommandResult(0, "")
        }
    }

    private fun verifyVipsthumbnailInput(fileName: String) {
        verify {
            commandRunner.run(
                match { it.first() == "vipsthumbnail" && it[1].endsWith(fileName) },
                any(),
            )
        }
    }

    // --- general behavior

    @Test
    fun `on generate returns false and does nothing when there is no source`() {
        givenSource(ThumbnailSource.None)

        val result = tested.generate(apod)

        assertFalse { result }
        verify(exactly = 0) { mediaFetcher.download(any(), any()) }
        verify(exactly = 0) { commandRunner.run(any(), any()) }
    }

    @Test
    fun `on generate writes date webp into the thumbnail folder and returns true`() {
        givenSource(ThumbnailSource.Image(imageUrl))
        givenDownloadWorks()
        givenVipsthumbnailWrites("webp-bytes")

        val result = tested.generate(apod)

        assertTrue { result }
        assertEquals("webp-bytes", Files.readString(thumbnailDir.resolve("2026-11-21.webp")))
    }

    @Test
    fun `on generate calls vipsthumbnail with the size limit and webp options`() {
        givenSource(ThumbnailSource.Image(imageUrl))
        givenDownloadWorks()
        givenVipsthumbnailWrites()

        tested.generate(apod)

        verify {
            commandRunner.run(
                listOf(
                    "vipsthumbnail",
                    downloadTarget.captured.toString(),
                    "--size",
                    "480x960>",
                    "-o",
                    "${thumbnailDir.resolve(".2026-11-21.tmp.webp")}[Q=70,keep=none]",
                ),
                any(),
            )
        }
    }

    @Test
    fun `on generate leaves only the final file in the thumbnail folder`() {
        givenSource(ThumbnailSource.Image(imageUrl))
        givenDownloadWorks()
        givenVipsthumbnailWrites()

        tested.generate(apod)

        assertEquals(
            listOf(thumbnailDir.resolve("2026-11-21.webp")),
            thumbnailDir.listDirectoryEntries(),
        )
    }

    @Test
    fun `on generate deletes the work folder after success`() {
        givenSource(ThumbnailSource.Image(imageUrl))
        givenDownloadWorks()
        givenVipsthumbnailWrites()

        tested.generate(apod)

        assertFalse { Files.exists(downloadTarget.captured.parent) }
    }

    @Test
    fun `on generate replaces an existing thumbnail`() {
        Files.createDirectories(thumbnailDir)
        Files.writeString(thumbnailDir.resolve("2026-11-21.webp"), "old")
        givenSource(ThumbnailSource.Image(imageUrl))
        givenDownloadWorks()
        givenVipsthumbnailWrites("new")

        tested.generate(apod)

        assertEquals("new", Files.readString(thumbnailDir.resolve("2026-11-21.webp")))
    }

    @Test
    fun `on generate throws and deletes the work folder when the download fails`() {
        givenSource(ThumbnailSource.Image(imageUrl))
        every { mediaFetcher.download(imageUrl, capture(downloadTarget)) } throws
            SourceNotFoundException("404")

        assertFailsWith<SourceNotFoundException> { tested.generate(apod) }
        assertFalse { Files.exists(downloadTarget.captured.parent) }
        verify(exactly = 0) { commandRunner.run(any(), any()) }
    }

    @Test
    fun `on generate throws and leaves no file when vipsthumbnail fails`() {
        givenSource(ThumbnailSource.Image(imageUrl))
        givenDownloadWorks()
        every { commandRunner.run(match { it.first() == "vipsthumbnail" }, any()) } answers {
            // A failing run can leave a partial file behind
            val output = firstArg<List<String>>().last().substringBefore("[")
            Files.writeString(Path.of(output), "half")
            CommandResult(255, "not a known file format")
        }

        val error = assertFailsWith<ThumbnailException> { tested.generate(apod) }

        assertTrue { error.message!!.contains("not a known file format") }
        assertEquals(emptyList(), thumbnailDir.listDirectoryEntries())
        assertFalse { Files.exists(downloadTarget.captured.parent) }
    }

    @Test
    fun `on generate throws when vipsthumbnail exits with 0 but writes nothing`() {
        givenSource(ThumbnailSource.Image(imageUrl))
        givenDownloadWorks()
        every { commandRunner.run(match { it.first() == "vipsthumbnail" }, any()) } returns
            CommandResult(0, "")

        assertFailsWith<ThumbnailException> { tested.generate(apod) }
        assertEquals(emptyList(), thumbnailDir.listDirectoryEntries())
    }

    @Test
    fun `on generate keeps the previous thumbnail when making a new one fails`() {
        Files.createDirectories(thumbnailDir)
        Files.writeString(thumbnailDir.resolve("2026-11-21.webp"), "old")
        givenSource(ThumbnailSource.Image(imageUrl))
        givenDownloadWorks()
        every { commandRunner.run(any(), any()) } returns CommandResult(1, "error")

        assertFailsWith<ThumbnailException> { tested.generate(apod) }

        assertEquals("old", Files.readString(thumbnailDir.resolve("2026-11-21.webp")))
    }

    // --- YouTube

    @Test
    fun `on generate uses the largest YouTube picture`() {
        givenSource(ThumbnailSource.YouTube("DzrCEm1ZBRY"))
        givenDownloadWorks()
        givenVipsthumbnailWrites()

        tested.generate(apod)

        verify {
            mediaFetcher.download("https://i.ytimg.com/vi/DzrCEm1ZBRY/maxresdefault.jpg", any())
        }
        verify(exactly = 0) { mediaFetcher.download(match { it.endsWith("sddefault.jpg") }, any()) }
    }

    @Test
    fun `on generate falls back to smaller YouTube pictures when the larger ones are missing`() {
        val base = "https://i.ytimg.com/vi/DzrCEm1ZBRY"
        givenSource(ThumbnailSource.YouTube("DzrCEm1ZBRY"))
        every { mediaFetcher.download("$base/maxresdefault.jpg", any()) } throws
            SourceNotFoundException("404")
        every { mediaFetcher.download("$base/sddefault.jpg", any()) } throws
            SourceNotFoundException("404")
        every { mediaFetcher.download("$base/hqdefault.jpg", capture(downloadTarget)) } answers {
            Files.writeString(downloadTarget.captured, "picture")
        }
        givenVipsthumbnailWrites()

        assertTrue { tested.generate(apod) }
        verifyOrder {
            mediaFetcher.download("$base/maxresdefault.jpg", any())
            mediaFetcher.download("$base/sddefault.jpg", any())
            mediaFetcher.download("$base/hqdefault.jpg", any())
        }
    }

    @Test
    fun `on generate throws SourceNotFoundException when a YouTube video has no picture`() {
        givenSource(ThumbnailSource.YouTube("M6-iC_aYcug"))
        every { mediaFetcher.download(any(), any()) } throws SourceNotFoundException("404")

        assertFailsWith<SourceNotFoundException> { tested.generate(apod) }
        verify(exactly = 0) { commandRunner.run(any(), any()) }
    }

    @Test
    fun `on generate does not try smaller YouTube pictures when a download fails for another reason`() {
        val base = "https://i.ytimg.com/vi/DzrCEm1ZBRY"
        givenSource(ThumbnailSource.YouTube("DzrCEm1ZBRY"))
        every { mediaFetcher.download("$base/maxresdefault.jpg", any()) } throws
            ThumbnailException("timeout")

        assertFailsWith<ThumbnailException> { tested.generate(apod) }
        verify(exactly = 0) { mediaFetcher.download("$base/sddefault.jpg", any()) }
    }

    // --- Vimeo

    @Test
    fun `on generate asks Vimeo oEmbed for the picture and downloads it`() {
        val picture = "https://i.vimeocdn.com/video/107303623-d_1280?region=us"
        givenSource(ThumbnailSource.Vimeo("16917950"))
        every {
            mediaFetcher.readText(
                "https://vimeo.com/api/oembed.json?url=https%3A%2F%2Fvimeo.com%2F16917950&width=1280",
            )
        } returns """{"type":"video","thumbnail_url":"$picture","thumbnail_width":1280}"""
        givenDownloadWorks()
        givenVipsthumbnailWrites()

        assertTrue { tested.generate(apod) }
        verify { mediaFetcher.download(picture, any()) }
    }

    @Test
    fun `on generate throws when the Vimeo answer has no thumbnail_url`() {
        givenSource(ThumbnailSource.Vimeo("16917950"))
        every { mediaFetcher.readText(any()) } returns """{"type":"video"}"""

        assertFailsWith<ThumbnailException> { tested.generate(apod) }
    }

    @Test
    fun `on generate throws when the Vimeo answer is not JSON`() {
        givenSource(ThumbnailSource.Vimeo("16917950"))
        every { mediaFetcher.readText(any()) } returns "<html>blocked</html>"

        assertFailsWith<ThumbnailException> { tested.generate(apod) }
    }

    // --- Video file (ffmpeg)

    @Test
    fun `on generate makes the thumbnail from the video frame at second 3`() {
        givenSource(ThumbnailSource.VideoFile(videoUrl))
        givenFfmpegFramesAt(3)
        givenVipsthumbnailWrites()

        assertTrue { tested.generate(apod) }
        verifyVipsthumbnailInput("frame-3.jpg")
    }

    @Test
    fun `on generate uses the first frame when the video is shorter than 3 seconds`() {
        givenSource(ThumbnailSource.VideoFile(videoUrl))
        givenFfmpegFramesAt(0)
        givenVipsthumbnailWrites()

        assertTrue { tested.generate(apod) }
        verifyVipsthumbnailInput("frame-0.jpg")
    }

    @Test
    fun `on generate throws when ffmpeg makes no frame`() {
        givenSource(ThumbnailSource.VideoFile(videoUrl))
        every { commandRunner.run(match { it.first() == "ffmpeg" }, any()) } returns
            CommandResult(1, "404 Not Found")

        assertFailsWith<ThumbnailException> { tested.generate(apod) }
        verify(exactly = 0) {
            commandRunner.run(match { it.first() == "vipsthumbnail" }, any())
        }
    }

    @Test
    fun `on generate passes the video url to ffmpeg after -i and allows only network protocols`() {
        givenSource(ThumbnailSource.VideoFile(videoUrl))
        givenFfmpegFramesAt(3)
        givenVipsthumbnailWrites()

        tested.generate(apod)

        verify {
            commandRunner.run(
                match {
                    it.first() == "ffmpeg" &&
                        it[it.indexOf("-i") + 1] == videoUrl &&
                        it[it.indexOf("-protocol_whitelist") + 1] == "https,tls,tcp,crypto" &&
                        it.indexOf("-ss") < it.indexOf("-i")
                },
                any(),
            )
        }
    }
}
