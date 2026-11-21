package dev.havir.hellouniverse.infrastructure.thumbnail

import java.nio.file.Path

/** Downloads source pictures and small API responses over HTTPS. */
interface MediaFetcher {
    /**
     * Saves the body of [url] to [target].
     * @throws SourceNotFoundException for HTTP 404 or 410.
     * @throws ThumbnailException for other errors or a too large body.
     */
    fun download(
        url: String,
        target: Path,
    )

    /** Reads a small text body, such as a JSON API response. */
    fun readText(url: String): String
}
