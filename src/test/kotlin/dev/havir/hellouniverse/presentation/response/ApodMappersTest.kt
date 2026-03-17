package dev.havir.hellouniverse.presentation.response

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.model.MediaType
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ApodMappersTest {
    @Test
    fun `on MediaType toResponse maps MediaType to MediaTypeResponse when MediaType is VIDEO`() {
        val mediaType = MediaType.VIDEO

        val result = mediaType.toResponse()

        assertEquals(MediaTypeResponse.VIDEO, result)
    }

    @Test
    fun `on MediaType toResponse maps MediaType to MediaTypeResponse when MediaType is IMAGE`() {
        val mediaType = MediaType.IMAGE

        val result = mediaType.toResponse()

        assertEquals(MediaTypeResponse.IMAGE, result)
    }

    @Test
    fun `on MediaType toResponse maps MediaType to MediaTypeResponse when MediaType is OTHER`() {
        val mediaType = MediaType.OTHER

        val result = mediaType.toResponse()

        assertEquals(MediaTypeResponse.OTHER, result)
    }

    @Test
    fun `on Apod toResponse maps Apod to ApodResponse`() {
        val apod = TestModels.apod(
            LocalDate.of(1800, 2, 10),
            "title-x",
            "explanation-y",
            MediaType.IMAGE,
            "url.de",
            "hd.com",
            null,
            null,
            "2"
        )

        val result = apod.toResponse()

        assertEquals(
            result, TestModels.apodResponse(
                LocalDate.of(1800, 2, 10),
                "title-x",
                "explanation-y",
                MediaTypeResponse.IMAGE,
                "url.de",
                "hd.com",
                null,
                null,
                "2"
            )
        )
    }

    @Test
    fun `on Apod List toResponse maps list of Apod to list of ApodResponse`() {
        val apods = listOf(
            TestModels.apod(
                LocalDate.of(2000, 2, 10),
                "title-1",
                "explanation-1",
                MediaType.OTHER,
                null,
                "hd.com",
                "thumb.com",
                "nasa",
                "33.vv"
            ), TestModels.apod(
                LocalDate.of(2500, 2, 10),
                "title-2",
                "explanation-2",
                MediaType.VIDEO,
                "url.x",
                "hd.x",
                null,
                null,
                "25.11"
            )
        )

        val result = apods.toResponse()

        assertEquals(
            result, listOf(
                TestModels.apodResponse(
                    LocalDate.of(2000, 2, 10),
                    "title-1",
                    "explanation-1",
                    MediaTypeResponse.OTHER,
                    null,
                    "hd.com",
                    "thumb.com",
                    "nasa",
                    "33.vv"
                ), TestModels.apodResponse(
                    LocalDate.of(2500, 2, 10),
                    "title-2",
                    "explanation-2",
                    MediaTypeResponse.VIDEO,
                    "url.x",
                    "hd.x",
                    null,
                    null,
                    "25.11"
                )
            )
        )
    }
}
