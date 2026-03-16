package dev.havir.hellouniverse.infrastructure.nasaapi.dto

import com.fasterxml.jackson.annotation.JsonProperty

enum class NasaMediaTypeDto {
    @JsonProperty("image")
    IMAGE,

    @JsonProperty("video")
    VIDEO,

    @JsonProperty("other")
    OTHER,
}
