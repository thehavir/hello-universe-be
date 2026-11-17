package dev.havir.hellouniverse.infrastructure.nasaapi.dto

/**
 * Error body from NASA's WordPress REST API.
 *
 * For example, `{"code":"apod_basic_not_found", ...}`.
 */
data class NasaErrorDto(
    val code: String,
)
