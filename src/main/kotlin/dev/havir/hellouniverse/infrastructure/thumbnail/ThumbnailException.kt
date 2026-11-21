package dev.havir.hellouniverse.infrastructure.thumbnail

/** A thumbnail could not be made. The APOD itself is not affected. */
open class ThumbnailException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

/** The source picture does not exist (HTTP 404 or 410). */
class SourceNotFoundException(
    message: String,
) : ThumbnailException(message)
