package dev.havir.hellouniverse.domain

import dev.havir.hellouniverse.domain.exception.ThumbnailGenerationException
import dev.havir.hellouniverse.domain.model.Apod

/** Makes our own small WebP thumbnail file for one APOD. */
interface ThumbnailGenerator {
    /**
     * @return true when the file was written, false when the APOD has no
     *   picture to make a thumbnail from (web apps, other media).
     * @throws ThumbnailGenerationException when a source exists but the file
     *   could not be made (download failed, tool failed, timeout, disk error).
     */
    fun generate(apod: Apod): Boolean
}
