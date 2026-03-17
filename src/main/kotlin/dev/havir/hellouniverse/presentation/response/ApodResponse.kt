package dev.havir.hellouniverse.presentation.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class ApodResponse(
    val date: LocalDate,

    val title: String,

    val explanation: String,

    @JsonProperty("media_type")
    val mediaType: MediaTypeResponse,

    val url: String?,

    @JsonProperty("hd_url")
    val hdUrl: String?,

    @JsonProperty("thumbnail_url")
    val thumbnailUrl: String?,

    val copyright: String?,

    @JsonProperty("service_version")
    val serviceVersion: String,
)
