package dev.havir.hellouniverse.infrastructure.nasaapi.media

import dev.havir.hellouniverse.domain.model.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Each test uses the real `basic_html` of one APOD, saved from the
 * 2026-11-16 snapshot of all APODs. Together they cover every form of
 * featured media found in that snapshot.
 */
class JsoupApodMediaExtractorTest {
    private val tested = JsoupApodMediaExtractor()

    @Test
    fun `on extract returns image inside a link`() {
        assertEquals(
            ApodMedia(
                MediaType.IMAGE,
                "$ASSETS/dynamicimage/assets/science/missions/webb/science" +
                    "/2026/10/weic2620a.jpg",
            ),
            extract("2026-11-16"),
        )
    }

    @Test
    fun `on extract returns image that has no link`() {
        assertEquals(
            ApodMedia(
                MediaType.IMAGE,
                "$ASSETS/dynamicimage/assets/science/cds/apod/apod/2026" +
                    "/november/Leonids2022_Hongyang_2880.jpg",
            ),
            extract("2026-11-15"),
        )
    }

    @Test
    fun `on extract returns animated gif as image`() {
        assertEquals(
            ApodMedia(
                MediaType.IMAGE,
                "$ASSETS/content/dam/science/cds/apod/apod/2025/august" +
                    "/perseid2025-cropped.gif",
            ),
            extract("2025-08-16"),
        )
    }

    @Test
    fun `on extract returns tif as image`() {
        assertEquals(
            ApodMedia(
                MediaType.IMAGE,
                "$ASSETS/content/dam/science/cds/apod/apod/2026/january" +
                    "/jupiter2026.tif",
            ),
            extract("2026-01-10"),
        )
    }

    @Test
    fun `on extract returns video file from video source`() {
        assertEquals(
            ApodMedia(
                MediaType.VIDEO,
                "$ASSETS/content/dam/science/cds/apod/apod/2026/october" +
                    "/occultation.mp4",
            ),
            extract("2026-10-28"),
        )
    }

    @Test
    fun `on extract adds https to YouTube iframe without scheme`() {
        assertEquals(
            ApodMedia(
                MediaType.VIDEO,
                "https://www.youtube.com/embed/DzrCEm1ZBRY?rel=0",
            ),
            extract("2026-10-18"),
        )
    }

    @Test
    fun `on extract returns Vimeo iframe as video with decoded url`() {
        // The HTML has `&#038;`; the url must have a plain `&`.
        assertEquals(
            ApodMedia(
                MediaType.VIDEO,
                "https://player.vimeo.com/video/108650530?title=0&byline=0" +
                    "&portrait=0&badge=0&color=ffffff",
            ),
            extract("2023-03-26"),
        )
    }

    @Test
    fun `on extract returns Ustream iframe as web app`() {
        assertEquals(
            ApodMedia(
                MediaType.WEB_APP,
                "https://www.ustream.tv/embed/17074538?v=3&wmode=direct" +
                    "&autoplay=true",
            ),
            extract("2019-06-02"),
        )
    }

    @Test
    fun `on extract returns YouTube nocookie iframe as video`() {
        assertEquals(
            ApodMedia(
                MediaType.VIDEO,
                "https://www.youtube-nocookie.com/embed/M6-iC_aYcug",
            ),
            tested.extract(
                "<iframe src='https://www.youtube-nocookie.com/embed/M6-iC_aYcug'>" +
                    "</iframe>",
            ),
        )
    }

    @Test
    fun `on extract returns interactive iframe as web app`() {
        assertEquals(
            ApodMedia(MediaType.WEB_APP, "https://earth.nullschool.net/"),
            extract("2016-10-10"),
        )
    }

    @Test
    fun `on extract keeps http iframe url as it is`() {
        assertEquals(
            ApodMedia(MediaType.WEB_APP, "http://apod.it/mapfull.shtml"),
            extract("2014-06-16"),
        )
    }

    @Test
    fun `on extract returns Flash iframe as other even on a web app host`() {
        assertEquals(
            ApodMedia(
                MediaType.OTHER,
                "https://apod.nasa.gov/image/0705/flvplayer.swf",
            ),
            extract("2007-05-22"),
        )
    }

    @Test
    fun `on extract returns null when NASA left out the image`() {
        // NASA's media_type is "image", but the page has no media element.
        assertNull(extract("2026-10-02"))
    }

    @Test
    fun `on extract returns null for an incomplete record`() {
        // Empty explanation and null media_type in NASA's data.
        assertNull(extract("2024-12-07"))
    }

    @Test
    fun `on extract returns null for empty html`() {
        assertNull(tested.extract(""))
    }

    // No real APOD has a relative media URL, so these use made-up html
    @Test
    fun `on extract throws for a relative image url`() {
        assertFailsWith<IllegalStateException> {
            tested.extract("<img src='/images/moon.jpg'>")
        }
    }

    @Test
    fun `on extract throws for a relative iframe url`() {
        assertFailsWith<IllegalStateException> {
            tested.extract("<iframe src='/embed/moon'></iframe>")
        }
    }

    private fun extract(date: String): ApodMedia? {
        val html =
            checkNotNull(javaClass.getResource("$FIXTURES/$date.html")) {
                "Missing fixture for $date"
            }.readText()
        return tested.extract(html)
    }

    private companion object {
        const val FIXTURES = "/nasa/apod-basic-html"
        const val ASSETS = "https://assets.science.nasa.gov"
    }
}
