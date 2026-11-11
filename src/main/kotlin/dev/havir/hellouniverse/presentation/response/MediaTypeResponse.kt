package dev.havir.hellouniverse.presentation.response

import com.fasterxml.jackson.annotation.JsonProperty

enum class MediaTypeResponse {
    @JsonProperty("image")
    IMAGE,

    @JsonProperty("video")
    VIDEO,

    @JsonProperty("web_app")
    WEB_APP,

    @JsonProperty("other")
    OTHER,
}
