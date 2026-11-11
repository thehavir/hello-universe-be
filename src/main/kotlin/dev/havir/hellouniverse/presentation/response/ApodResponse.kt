package dev.havir.hellouniverse.presentation.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * APOD content served to clients.
 *
 * [explanation], [credit] and [copyright] are HTML fragments.
 * [url] is the primary media URL: the original image, a video URL,
 * a web app URL, or another supported external resource.
 * [thumbnailUrl] is a generated WebP preview, when available.
 * [sourceUrl] points to the original NASA article.
 */
data class ApodResponse(
    val date: LocalDate,
    val title: String,
    val explanation: String,
    val credit: String?,
    val copyright: String?,
    @JsonProperty("media_type")
    val mediaType: MediaTypeResponse,
    val url: String?,
    @JsonProperty("thumbnail_url")
    val thumbnailUrl: String?,
    @JsonProperty("source_url")
    val sourceUrl: String?,
)
