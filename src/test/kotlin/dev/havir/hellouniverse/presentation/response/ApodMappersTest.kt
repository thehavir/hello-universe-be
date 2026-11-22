package dev.havir.hellouniverse.presentation.response

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.model.MediaType
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApodMappersTest {
    val thumbnailUrls = ThumbnailUrls("https://media.test/media")

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
        val apod =
            TestModels.apod(
                LocalDate.of(1800, 2, 10),
                "title-x",
                "explanation-y",
                MediaType.IMAGE,
                "url.de",
                "hd.com",
                null,
                "2",
            )

        val result = apod.toResponse()

        assertEquals(
            TestModels.apodResponse(
                LocalDate.of(1800, 2, 10),
                "title-x",
                "explanation-y",
                MediaTypeResponse.IMAGE,
                "url.de",
                "hd.com",
                null,
                null,
                "2",
            ),
            result,
        )
    }

    @Test
    fun `on Apod List toResponse maps list of Apod to list of ApodResponse`() {
        val apods =
            listOf(
                TestModels.apod(
                    LocalDate.of(2000, 2, 10),
                    "title-1",
                    "explanation-1",
                    MediaType.OTHER,
                    null,
                    "hd.com",
                    "thumb.com",
                    "33.vv",
                ),
                TestModels.apod(
                    LocalDate.of(2500, 2, 10),
                    "title-2",
                    "explanation-2",
                    MediaType.VIDEO,
                    "url.x",
                    "hd.x",
                    null,
                    "25.11",
                ),
            )

        val result =
            apods
                .map { TestModels.apodWithThumbnail(it) }
                .toResponse(thumbnailUrls)

        assertEquals(
            listOf(
                TestModels.apodResponse(
                    LocalDate.of(2000, 2, 10),
                    "title-1",
                    "explanation-1",
                    MediaTypeResponse.OTHER,
                    null,
                    "hd.com",
                    "thumb.com",
                    null,
                    "33.vv",
                ),
                TestModels.apodResponse(
                    LocalDate.of(2500, 2, 10),
                    "title-2",
                    "explanation-2",
                    MediaTypeResponse.VIDEO,
                    "url.x",
                    "hd.x",
                    null,
                    null,
                    "25.11",
                ),
            ),
            result,
        )
    }

    @Test
    fun `on ApodWithThumbnail toResponse sets thumbnail url built by ThumbnailUrls when thumbnail exists`() {
        val date = LocalDate.of(2026, 11, 21)
        val apodWithThumbnail =
            TestModels.apodWithThumbnail(
                TestModels.apod(date = date, title = "title-t"),
                TestModels.apodThumbnail(
                    date,
                    Instant.parse("2026-11-21T10:15:30Z"),
                ),
            )

        val result = apodWithThumbnail.toResponse(thumbnailUrls)

        assertEquals(
            TestModels.apodResponse(
                date = date,
                title = "title-t",
                thumbnailUrl =
                    "https://media.test/media/thumb/2026-11-21.webp?v=1795256130",
            ),
            result,
        )
    }

    @Test
    fun `on ApodWithThumbnail toResponse sets thumbnail url to null when thumbnail is null`() {
        val apodWithThumbnail = TestModels.apodWithThumbnail(thumbnail = null)

        val result = apodWithThumbnail.toResponse(thumbnailUrls)

        assertNull(result.thumbnailUrl)
    }

    @Test
    fun `on ApodWithThumbnail List toResponse maps each item and keeps the order`() {
        val date1 = LocalDate.of(2026, 11, 20)
        val date2 = LocalDate.of(2026, 11, 21)
        val items =
            listOf(
                TestModels.apodWithThumbnail(
                    TestModels.apod(date = date1),
                    null,
                ),
                TestModels.apodWithThumbnail(
                    TestModels.apod(date = date2),
                    TestModels.apodThumbnail(
                        date2,
                        Instant.parse("2026-11-21T10:15:31Z"),
                    ),
                ),
            )

        val result = items.toResponse(thumbnailUrls)

        assertEquals(
            listOf(
                TestModels.apodResponse(date = date1),
                TestModels.apodResponse(
                    date = date2,
                    thumbnailUrl =
                        "https://media.test/media/thumb/2026-11-21.webp?v=1795256131",
                ),
            ),
            result,
        )
    }
}
