package dev.havir.hellouniverse.infrastructure.thumbnail

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.model.MediaType
import kotlin.test.Test
import kotlin.test.assertEquals

class ThumbnailSourceResolverTest {
    val tested = ThumbnailSourceResolver()

    private fun resolve(
        mediaType: MediaType,
        url: String?,
    ) = tested.resolve(TestModels.apod(mediaType = mediaType, url = url))

    @Test
    fun `on resolve returns Image with the url when media type is IMAGE`() {
        val url =
            "https://assets.science.nasa.gov/apod/2016/AirglowFan_960.jpg?w=960&h=640"

        assertEquals(ThumbnailSource.Image(url), resolve(MediaType.IMAGE, url))
    }

    @Test
    fun `on resolve returns None when url uses http`() {
        val result =
            resolve(MediaType.IMAGE, "http://nusoft.fnal.gov/nova/img/FD.gif")

        assertEquals(ThumbnailSource.None, result)
    }

    @Test
    fun `on resolve returns None when url is null`() {
        assertEquals(ThumbnailSource.None, resolve(MediaType.IMAGE, null))
    }

    @Test
    fun `on resolve returns None when url is blank`() {
        assertEquals(ThumbnailSource.None, resolve(MediaType.IMAGE, "  "))
    }

    @Test
    fun `on resolve returns None when url is not a valid URI`() {
        assertEquals(ThumbnailSource.None, resolve(MediaType.IMAGE, "https://a b"))
    }

    @Test
    fun `on resolve returns YouTube with the video id for a youtube embed url`() {
        val result =
            resolve(
                MediaType.VIDEO,
                "https://www.youtube.com/embed/DzrCEm1ZBRY?rel=0",
            )

        assertEquals(ThumbnailSource.YouTube("DzrCEm1ZBRY"), result)
    }

    @Test
    fun `on resolve returns YouTube for a youtube embed url without www`() {
        val result =
            resolve(MediaType.VIDEO, "https://youtube.com/embed/LtKUuAM3zDY")

        assertEquals(ThumbnailSource.YouTube("LtKUuAM3zDY"), result)
    }

    @Test
    fun `on resolve returns YouTube for a protocol-relative youtube url`() {
        val result =
            resolve(
                MediaType.VIDEO,
                "//www.youtube.com/embed/DmYK479EpQc?vq=hd720&rel=0",
            )

        assertEquals(ThumbnailSource.YouTube("DmYK479EpQc"), result)
    }

    @Test
    fun `on resolve returns YouTube for a youtube-nocookie embed url`() {
        val result =
            resolve(
                MediaType.VIDEO,
                "https://www.youtube-nocookie.com/embed/M6-iC_aYcug",
            )

        assertEquals(ThumbnailSource.YouTube("M6-iC_aYcug"), result)
    }

    @Test
    fun `on resolve returns Vimeo with the video id for a vimeo player url`() {
        val result =
            resolve(
                MediaType.VIDEO,
                "https://player.vimeo.com/video/11386048?title=0&byline=0",
            )

        assertEquals(ThumbnailSource.Vimeo("11386048"), result)
    }

    @Test
    fun `on resolve returns Vimeo for a protocol-relative vimeo url`() {
        val result =
            resolve(MediaType.VIDEO, "//player.vimeo.com/video/80568171?portrait=0")

        assertEquals(ThumbnailSource.Vimeo("80568171"), result)
    }

    @Test
    fun `on resolve returns VideoFile when a VIDEO url is not YouTube or Vimeo`() {
        val url =
            "https://assets.science.nasa.gov/apod/2026/october/occultation.mp4"

        assertEquals(ThumbnailSource.VideoFile(url), resolve(MediaType.VIDEO, url))
    }

    @Test
    fun `on resolve returns VideoFile when a youtube url has no embed path`() {
        // Not produced by the media extractor; kept as a file so a mistake fails loudly in ffmpeg
        val url = "https://www.youtube.com/watch?v=DzrCEm1ZBRY"

        assertEquals(ThumbnailSource.VideoFile(url), resolve(MediaType.VIDEO, url))
    }

    @Test
    fun `on resolve returns None when media type is WEB_APP`() {
        assertEquals(
            ThumbnailSource.None,
            resolve(MediaType.WEB_APP, "https://earth.nullschool.net/"),
        )
    }

    @Test
    fun `on resolve returns None when media type is OTHER`() {
        assertEquals(
            ThumbnailSource.None,
            resolve(MediaType.OTHER, "https://apod.nasa.gov/apod/image/a.swf"),
        )
    }
}
