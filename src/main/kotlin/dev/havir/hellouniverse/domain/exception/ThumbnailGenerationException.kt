package dev.havir.hellouniverse.domain.exception

/**
 * A thumbnail could not be made for an APOD that has a source picture
 * (download failed, tool failed, timeout). The APOD itself is not affected.
 */
open class ThumbnailGenerationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
