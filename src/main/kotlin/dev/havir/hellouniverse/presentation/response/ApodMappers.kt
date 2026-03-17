package dev.havir.hellouniverse.presentation.response

import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType

fun List<Apod>.toResponse(): List<ApodResponse> = map { it.toResponse() }

fun Apod.toResponse(): ApodResponse = ApodResponse(
    date = date,
    title = title,
    explanation = explanation,
    mediaType = mediaType.toResponse(),
    url = url,
    hdUrl = hdUrl,
    thumbnailUrl = thumbnailUrl,
    copyright = copyright,
    serviceVersion = serviceVersion
)

fun MediaType.toResponse(): MediaTypeResponse {
    return when (this) {
        MediaType.IMAGE -> MediaTypeResponse.IMAGE
        MediaType.VIDEO -> MediaTypeResponse.VIDEO
        MediaType.OTHER -> MediaTypeResponse.OTHER
    }
}
