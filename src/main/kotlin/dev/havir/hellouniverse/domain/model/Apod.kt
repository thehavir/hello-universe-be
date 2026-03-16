package dev.havir.hellouniverse.domain.model

import java.time.LocalDate

data class Apod(
    val date: LocalDate,
    val title: String,
    val explanation: String,
    val mediaType: MediaType,
    val url: String?,
    val hdUrl: String?,
    val thumbnailUrl: String?,
    val copyright: String?,
    val serviceVersion: String,
)
