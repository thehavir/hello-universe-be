package dev.havir.hellouniverse.infrastructure.nasaapi.media

/**
 * Finds the featured media (image, video or web app) of one APOD
 * in its APOD Basic HTML document.
 */
interface ApodMediaExtractor {
    /**
     * @param html the full `basic_html` document from NASA.
     * @return the media's type and absolute URL, or null when the
     *   document contains no media.
     */
    fun extract(html: String): ApodMedia?
}
