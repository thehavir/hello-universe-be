package dev.havir.hellouniverse.infrastructure.nasaapi.media

import dev.havir.hellouniverse.domain.model.MediaType

data class ApodMedia(
    val type: MediaType,
    val url: String,
)
