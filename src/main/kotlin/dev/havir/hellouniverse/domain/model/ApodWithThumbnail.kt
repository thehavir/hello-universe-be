package dev.havir.hellouniverse.domain.model

/**
 * An [Apod] together with its generated thumbnail, if there is one.
 *
 * [thumbnail] is null when no thumbnail exists: the APOD has no usable
 * source picture (web apps and other embedded content), or its thumbnail
 * has not been generated yet.
 */
data class ApodWithThumbnail(
    val apod: Apod,
    val thumbnail: ApodThumbnail?,
)
