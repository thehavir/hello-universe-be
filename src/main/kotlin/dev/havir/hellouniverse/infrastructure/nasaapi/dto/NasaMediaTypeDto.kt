package dev.havir.hellouniverse.infrastructure.nasaapi.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Media labels supplied by NASA's APOD Basic API.
 * The `iframe` label describes embedded content, which may be a video
 * player or an interactive web app.
 */
enum class NasaMediaTypeDto {
    /** Image content, including animated images. */
    @JsonProperty("image")
    IMAGE,

    /** Video content, delivered as a file, stream, or embedded player. */
    @JsonProperty("video")
    VIDEO,

    /** Content embedded in an iframe, including players and web apps. */
    @JsonProperty("iframe")
    IFRAME,

    /** An explicit upstream `other` value; missing values remain null. */
    @JsonProperty("other")
    OTHER,
}
