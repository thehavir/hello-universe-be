package dev.havir.hellouniverse.presentation.response

import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType

fun List<Apod>.toResponse(): List<ApodResponse> = map { it.toResponse() }

fun Apod.toResponse(): ApodResponse =
    ApodResponse(
        date = date,
        title = title,
        explanation = explanation,
        credit = credit,
        copyright = copyright,
        mediaType = mediaType.toResponse(),
        url = url,
        thumbnailUrl = null,
        sourceUrl = sourceUrl,
    )

fun MediaType.toResponse(): MediaTypeResponse =
    when (this) {
        MediaType.IMAGE -> MediaTypeResponse.IMAGE
        MediaType.VIDEO -> MediaTypeResponse.VIDEO
        MediaType.WEB_APP -> MediaTypeResponse.WEB_APP
        MediaType.OTHER -> MediaTypeResponse.OTHER
    }
