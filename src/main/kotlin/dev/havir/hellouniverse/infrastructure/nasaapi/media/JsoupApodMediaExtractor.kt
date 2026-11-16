package dev.havir.hellouniverse.infrastructure.nasaapi.media

import dev.havir.hellouniverse.domain.model.MediaType
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.net.URI

/**
 * Finds the featured media in an APOD Basic HTML document.
 *
 * The rules come from all 11,469 APODs until 2026-11-16 from NASA data. The
 * media is the first of these elements in the page:
 * - `<img src>`: an image. Always an absolute `https://` URL.
 * - `<source src>` inside `<video>`: a video file. Always `https://`.
 * - `<iframe src>`: a video player or a web app. Starts with `https://`,
 *   `http://` or `//`.
 *
 * 33 documents contain none of these, so [extract] returns null for them.
 */
@Component
class JsoupApodMediaExtractor : ApodMediaExtractor {
    override fun extract(html: String): ApodMedia? {
        val element =
            Jsoup.parse(html).body().selectFirst(MEDIA_SELECTOR) ?: return null
        val url = withScheme(element.attr("src"))

        val type =
            when (element.normalName()) {
                "img" -> MediaType.IMAGE
                "source" -> MediaType.VIDEO
                else -> iframeType(url)
            }

        return ApodMedia(type, url)
    }

    private fun withScheme(url: String): String =
        if (url.startsWith("//")) "https:$url" else url

    private fun iframeType(url: String): MediaType {
        // Old Flash players
        if (url.substringBefore('?').endsWith(".swf")) {
            return MediaType.OTHER
        }

        return when (URI(url).host.removePrefix("www.")) {
            in VIDEO_HOSTS -> MediaType.VIDEO
            in WEB_APP_HOSTS -> MediaType.WEB_APP
            else -> MediaType.OTHER
        }
    }

    private companion object {
        const val MEDIA_SELECTOR = "img[src], video > source[src], iframe[src]"

        val VIDEO_HOSTS =
            setOf(
                "youtube.com",
                "player.vimeo.com",
                "ustream.tv",
            )

        val WEB_APP_HOSTS =
            setOf(
                "apod.it",
                "apod.nasa.gov",
                "astrobin.com",
                "earth.nullschool.net",
                "mars.nasa.gov",
                "meteorshowers.org",
                "sci.esa.int",
                "solarsystem.nasa.gov",
            )
    }
}
