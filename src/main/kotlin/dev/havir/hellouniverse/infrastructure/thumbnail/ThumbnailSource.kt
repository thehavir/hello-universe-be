package dev.havir.hellouniverse.infrastructure.thumbnail

import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URISyntaxException

/** Where the picture for a thumbnail comes from. */
sealed interface ThumbnailSource {
    /** An image file, downloaded as it is. */
    data class Image(
        val url: String,
    ) : ThumbnailSource

    /** A video file; one frame is taken with ffmpeg. */
    data class VideoFile(
        val url: String,
    ) : ThumbnailSource

    /** A YouTube video; its preview picture is used. */
    data class YouTube(
        val videoId: String,
    ) : ThumbnailSource

    /** A Vimeo video; its preview picture is used. */
    data class Vimeo(
        val videoId: String,
    ) : ThumbnailSource

    /** No picture: web apps, other media, unknown video hosts. */
    data object None : ThumbnailSource
}

/**
 * Chooses the source picture for an APOD. It trusts [Apod.mediaType], which
 * the media extractor sets when the APOD is fetched: a VIDEO is a YouTube or
 * Vimeo player, or otherwise a video file. Only HTTPS URLs are used; old
 * rows can have protocol-relative URLs (`//www.youtube.com/...`), which are
 * read as HTTPS.
 */
@Component
class ThumbnailSourceResolver {
    fun resolve(apod: Apod): ThumbnailSource {
        val uri = apod.url?.let(::httpsUri) ?: return ThumbnailSource.None
        return when (apod.mediaType) {
            MediaType.IMAGE -> ThumbnailSource.Image(uri.toString())
            MediaType.VIDEO -> videoSource(uri)
            MediaType.WEB_APP, MediaType.OTHER -> ThumbnailSource.None
        }
    }

    private fun videoSource(uri: URI): ThumbnailSource =
        videoId(uri, YOUTUBE_HOSTS, YOUTUBE_PATH)?.let(ThumbnailSource::YouTube)
            ?: videoId(
                uri,
                VIMEO_HOSTS,
                VIMEO_PATH,
            )?.let(ThumbnailSource::Vimeo)
            ?: ThumbnailSource.VideoFile(uri.toString())

    private fun videoId(
        uri: URI,
        hosts: Set<String>,
        path: Regex,
    ): String? =
        path
            .matchEntire(uri.path.orEmpty())
            ?.groupValues
            ?.get(1)
            ?.takeIf { uri.host.lowercase().removePrefix("www.") in hosts }

    private fun httpsUri(url: String): URI? {
        val trimmed = url.trim()
        val withScheme =
            if (trimmed.startsWith("//")) "https:$trimmed" else trimmed
        val uri =
            try {
                URI(withScheme)
            } catch (_: URISyntaxException) {
                return null
            }
        return uri.takeIf { it.scheme == "https" && !it.host.isNullOrBlank() }
    }

    private companion object {
        val YOUTUBE_HOSTS = setOf("youtube.com", "youtube-nocookie.com")
        val VIMEO_HOSTS = setOf("player.vimeo.com")
        val YOUTUBE_PATH = Regex("/embed/([A-Za-z0-9_-]{11})/?")
        val VIMEO_PATH = Regex("/video/([0-9]+)/?")
    }
}
