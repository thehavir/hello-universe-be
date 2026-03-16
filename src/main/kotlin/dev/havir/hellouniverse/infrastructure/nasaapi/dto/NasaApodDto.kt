package dev.havir.hellouniverse.infrastructure.nasaapi.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class NasaApodDto(
    val date: LocalDate,

    val title: String,

    val explanation: String,

    @JsonProperty("media_type")
    val mediaType: NasaMediaTypeDto,

    val url: String?,

    @JsonProperty("hdurl")
    val hdUrl: String?,

    @JsonProperty("thumbnail_url")
    val thumbnailUrl: String?,

    val copyright: String?,

    @JsonProperty("service_version")
    val serviceVersion: String,
)
