package dev.havir.hellouniverse.presentation.response

import dev.havir.hellouniverse.domain.model.ApodThumbnail
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Builds the public URL of a thumbnail file. The version in the URL changes
 * when the file is generated again, so clients that cache it for a long time
 * load the new file.
 *
 * The file name `<date>.webp` must match what `VipsThumbnailGenerator`
 * (infrastructure) writes; change both together.
 */
@Component
class ThumbnailUrls(
    @Value($$"${media.base-url}") baseUrl: String,
) {
    private val baseUrl = baseUrl.trimEnd('/')

    fun of(thumbnail: ApodThumbnail): String =
        "$baseUrl/thumb/${thumbnail.date}.webp" +
            "?v=${thumbnail.generatedAt.epochSecond}"
}
