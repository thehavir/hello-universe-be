package dev.havir.hellouniverse.presentation.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,

    @JsonProperty("timestamp")
    val timestamp: LocalDateTime = LocalDateTime.now(),
)
