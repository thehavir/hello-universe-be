package dev.havir.hellouniverse.infrastructure.thumbnail

import dev.havir.hellouniverse.domain.ThumbnailGenerator
import dev.havir.hellouniverse.domain.model.Apod
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import tools.jackson.core.JacksonException
import tools.jackson.databind.json.JsonMapper
import java.io.IOException
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.time.Duration

/**
 * Writes `<thumbnail-dir>/<date>.webp`, at most 480x960 and never enlarged.
 * `ThumbnailUrls` (presentation) builds the public URL from the same
 * `<date>.webp` name; change both together.
 *
 * The source picture is downloaded (or, for a video file, one frame is taken
 * with ffmpeg) into a temporary folder that is always deleted. The WebP is
 * first written as `.<date>.tmp.webp` in the thumbnail folder and then
 * renamed, so nginx never serves a half-written file. Writing in the same
 * folder also gives the file the folder's group (www-data on the server), so
 * nginx can read it.
 */
@Component
class VipsThumbnailGenerator(
    private val sourceResolver: ThumbnailSourceResolver,
    private val mediaFetcher: MediaFetcher,
    private val commandRunner: CommandRunner,
    private val jsonMapper: JsonMapper,
    @Value($$"${media.thumbnail-dir}") thumbnailDir: String,
) : ThumbnailGenerator {
    // Must be an absolute path. With a relative path, vipsthumbnail saves the
    // thumbnail in the wrong folder (next to the source picture), or, if the
    // path starts with ".", prints it to the console and saves no file.
    private val thumbnailDir: Path = Path.of(thumbnailDir).toAbsolutePath()

    override fun generate(apod: Apod): Boolean {
        val source = sourceResolver.resolve(apod)
        if (source == ThumbnailSource.None) return false

        try {
            write(apod, source)
        } catch (e: IOException) {
            throw ThumbnailException(
                "Cannot write the thumbnail of ${apod.date}",
                e,
            )
        }
        return true
    }

    private fun write(
        apod: Apod,
        source: ThumbnailSource,
    ) {
        Files.createDirectories(thumbnailDir)
        val workDir = Files.createTempDirectory("thumbnail-")
        val partial = thumbnailDir.resolve(".${apod.date}.tmp.webp")
        try {
            val input = prepareInput(source, workDir)
            writeWebp(input, partial)
            Files.move(
                partial,
                thumbnailDir.resolve("${apod.date}.webp"),
                ATOMIC_MOVE,
                REPLACE_EXISTING,
            )
        } finally {
            workDir.toFile().deleteRecursively()
            Files.deleteIfExists(partial)
        }
    }

    private fun prepareInput(
        source: ThumbnailSource,
        workDir: Path,
    ): Path =
        when (source) {
            is ThumbnailSource.Image -> {
                download(source.url, workDir)
            }

            is ThumbnailSource.VideoFile -> {
                videoFrame(source.url, workDir)
            }

            is ThumbnailSource.YouTube -> {
                youTubePicture(source.videoId, workDir)
            }

            is ThumbnailSource.Vimeo -> {
                download(vimeoPictureUrl(source.videoId), workDir)
            }

            ThumbnailSource.None -> {
                throw IllegalArgumentException("No source to prepare")
            }
        }

    private fun download(
        url: String,
        workDir: Path,
    ): Path {
        val target = workDir.resolve("source")
        mediaFetcher.download(url, target)
        return target
    }

    // Not every video has every size; a missing one answers 404
    private fun youTubePicture(
        videoId: String,
        workDir: Path,
    ): Path =
        YOUTUBE_PICTURES.firstNotNullOfOrNull { name ->
            try {
                download("https://i.ytimg.com/vi/$videoId/$name.jpg", workDir)
            } catch (_: SourceNotFoundException) {
                null
            }
        }
            ?: throw SourceNotFoundException(
                "YouTube video $videoId has no picture",
            )

    // Unlike YouTube, a Vimeo picture URL has a random hash that can't be
    // built from the video id. Vimeo's public oEmbed API returns it as
    // `thumbnail_url`; `width=1280` asks for the large picture.
    private fun vimeoPictureUrl(videoId: String): String {
        val videoUrl =
            URLEncoder.encode("https://vimeo.com/$videoId", Charsets.UTF_8)
        val json =
            mediaFetcher.readText(
                "https://vimeo.com/api/oembed.json?url=$videoUrl&width=1280",
            )
        val pictureUrl =
            try {
                jsonMapper.readTree(json).path("thumbnail_url").stringValue()
            } catch (e: JacksonException) {
                throw ThumbnailException("Invalid Vimeo answer for $videoId", e)
            }
        return pictureUrl?.takeIf { it.isNotBlank() }
            ?: throw ThumbnailException("Vimeo video $videoId has no picture")
    }

    // A video shorter than FRAME_SECOND gives no frame; then try the start
    private fun videoFrame(
        url: String,
        workDir: Path,
    ): Path =
        listOf(FRAME_SECOND, 0).firstNotNullOfOrNull { second ->
            val frame = workDir.resolve("frame-$second.jpg")
            val result = commandRunner.run(ffmpeg(url, second, frame), TIMEOUT)
            frame.takeIf { result.succeeded && isNotEmpty(it) }
        } ?: throw ThumbnailException("ffmpeg made no frame for $url")

    private fun ffmpeg(
        url: String,
        second: Int,
        frame: Path,
    ): List<String> =
        listOf(
            "ffmpeg",
            "-nostdin",
            "-hide_banner",
            "-loglevel",
            "error",
            // Network input only; never read local files
            "-protocol_whitelist",
            "https,tls,tcp,crypto",
            "-threads",
            "1",
            "-an",
            "-sn",
            // Before -i: seek by reading only the needed part of the video
            "-ss",
            second.toString(),
            "-i",
            url,
            "-frames:v",
            "1",
            "-q:v",
            "2",
            "-y",
            frame.toString(),
        )

    private fun writeWebp(
        input: Path,
        output: Path,
    ) {
        val result =
            commandRunner.run(
                listOf(
                    "vipsthumbnail",
                    input.toString(),
                    "--size",
                    SIZE,
                    "-o",
                    "$output[$WEBP_OPTIONS]",
                ),
                TIMEOUT,
            )
        if (!result.succeeded || !isNotEmpty(output)) {
            throw ThumbnailException("vipsthumbnail failed: ${result.output}")
        }
    }

    private fun isNotEmpty(file: Path): Boolean =
        Files.isRegularFile(file) && Files.size(file) > 0

    private companion object {
        // At most 480 wide and 960 high; ">" means only make smaller
        const val SIZE = "480x960>"

        // keep=none drops metadata such as EXIF (needs libvips 8.15+)
        const val WEBP_OPTIONS = "Q=70,keep=none"
        const val FRAME_SECOND = 3
        val TIMEOUT: Duration = Duration.ofSeconds(60)

        // Largest first: 1280x720, 640x480, 480x360
        val YOUTUBE_PICTURES = listOf("maxresdefault", "sddefault", "hqdefault")
    }
}
