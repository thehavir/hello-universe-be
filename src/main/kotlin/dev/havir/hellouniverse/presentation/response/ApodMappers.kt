package dev.havir.hellouniverse.presentation.response

import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.ApodWithThumbnail
import dev.havir.hellouniverse.domain.model.MediaType

fun List<ApodWithThumbnail>.toResponse(
    thumbnailUrls: ThumbnailUrls,
): List<ApodResponse> = map { it.toResponse(thumbnailUrls) }

fun ApodWithThumbnail.toResponse(thumbnailUrls: ThumbnailUrls): ApodResponse =
    apod.toResponse(thumbnail?.let(thumbnailUrls::of))

fun Apod.toResponse(thumbnailUrl: String? = null): ApodResponse =
    ApodResponse(
        date = date,
        title = title,
        explanation = explanation,
        credit = credit,
        copyright = copyright,
        mediaType = mediaType.toResponse(),
        url = url,
        thumbnailUrl = thumbnailUrl,
        sourceUrl = sourceUrl,
    )

fun MediaType.toResponse(): MediaTypeResponse =
    when (this) {
        MediaType.IMAGE -> MediaTypeResponse.IMAGE
        MediaType.VIDEO -> MediaTypeResponse.VIDEO
        MediaType.WEB_APP -> MediaTypeResponse.WEB_APP
        MediaType.OTHER -> MediaTypeResponse.OTHER
    }
