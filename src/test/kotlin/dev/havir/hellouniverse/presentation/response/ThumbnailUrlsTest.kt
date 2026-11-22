package dev.havir.hellouniverse.presentation.response

import dev.havir.hellouniverse.TestModels
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ThumbnailUrlsTest {
    @Test
    fun `on of returns base url, thumb folder, date as webp file name and generatedAt seconds as version`() {
        val tested = ThumbnailUrls("https://media.test/media")
        val thumbnail =
            TestModels.apodThumbnail(
                LocalDate.of(2026, 11, 21),
                Instant.parse("2026-11-21T10:15:30Z"),
            )

        val result = tested.of(thumbnail)

        assertEquals(
            "https://media.test/media/thumb/2026-11-21.webp?v=1795256130",
            result,
        )
    }

    @Test
    fun `on of does not duplicate the slash when base url ends with a slash`() {
        val tested = ThumbnailUrls("https://media.test/media/")
        val thumbnail =
            TestModels.apodThumbnail(
                LocalDate.of(1995, 6, 16),
                Instant.parse("2026-11-21T10:15:30Z"),
            )

        val result = tested.of(thumbnail)

        assertEquals(
            "https://media.test/media/thumb/1995-06-16.webp?v=1795256130",
            result,
        )
    }

    @Test
    fun `on of returns a different url when the thumbnail was generated again`() {
        val tested = ThumbnailUrls("https://media.test/media")
        val date = LocalDate.of(2026, 11, 21)
        val first =
            TestModels.apodThumbnail(date, Instant.parse("2026-11-21T10:15:30Z"))
        val second =
            TestModels.apodThumbnail(date, Instant.parse("2026-11-22T08:00:00Z"))

        assertNotEquals(tested.of(first), tested.of(second))
    }

    @Test
    fun `on of ignores the fraction of a second in generatedAt`() {
        val tested = ThumbnailUrls("https://media.test/media")
        val date = LocalDate.of(2026, 11, 21)
        val thumbnail =
            TestModels.apodThumbnail(
                date,
                Instant.parse("2026-11-21T10:15:30.987Z"),
            )

        val result = tested.of(thumbnail)

        assertEquals(
            "https://media.test/media/thumb/2026-11-21.webp?v=1795256130",
            result,
        )
    }
}
