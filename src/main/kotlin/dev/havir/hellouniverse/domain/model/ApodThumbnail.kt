package dev.havir.hellouniverse.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Records that the thumbnail image of the [Apod] for [date] has been
 * generated, and when ([generatedAt]).
 *
 * It holds no URL: the file name comes from [date], and the public address
 * comes from configuration. A new [generatedAt] marks a regenerated file, so
 * clients can fetch it again instead of using a cached copy.
 */
data class ApodThumbnail(
    val date: LocalDate,
    val generatedAt: Instant,
)
