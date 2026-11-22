package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.application.usecase.GetApodsByDateRangeUseCase.Companion.MAX_RANGE_DAYS
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.ApodThumbnailRepository
import dev.havir.hellouniverse.domain.exception.DateRangeTooLargeException
import dev.havir.hellouniverse.domain.exception.StartDateAfterEndDateException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetApodsByDateRangeUseCaseTest {
    lateinit var apodRepository: ApodRepository
    lateinit var apodThumbnailRepository: ApodThumbnailRepository
    lateinit var tested: GetApodsByDateRangeUseCase

    @BeforeEach
    fun setUp() {
        apodRepository = mockk()
        apodThumbnailRepository = mockk()
        tested =
            GetApodsByDateRangeUseCase(apodRepository, apodThumbnailRepository)
    }

    @Test
    fun `on execute calls findByDateRange on the ApodRepository`() {
        val startDate = LocalDate.of(1970, 1, 1)
        val endDate = LocalDate.of(1970, 2, 1)
        every {
            apodRepository.findByDateRange(startDate, endDate)
        } returns listOf(TestModels.apod())
        every {
            apodThumbnailRepository.findByDateRange(startDate, endDate)
        } returns emptyList()

        tested.execute(startDate, endDate)

        verify { apodRepository.findByDateRange(startDate, endDate) }
    }

    @Test
    fun `on execute calls findByDateRange on the ApodThumbnailRepository`() {
        val startDate = LocalDate.of(1970, 1, 1)
        val endDate = LocalDate.of(1970, 2, 1)
        every {
            apodRepository.findByDateRange(startDate, endDate)
        } returns listOf(TestModels.apod())
        every {
            apodThumbnailRepository.findByDateRange(startDate, endDate)
        } returns emptyList()

        tested.execute(startDate, endDate)

        verify { apodThumbnailRepository.findByDateRange(startDate, endDate) }
    }

    @Test
    fun `on execute throws when ApodRepository throws`() {
        val startDate = LocalDate.of(1970, 1, 1)
        val endDate = LocalDate.of(1970, 2, 1)
        val error = "Test error 1"
        every {
            apodRepository.findByDateRange(startDate, endDate)
        } throws RuntimeException(error)
        every {
            apodThumbnailRepository.findByDateRange(startDate, endDate)
        } returns emptyList()

        assertFailsWith<RuntimeException> { tested.execute(startDate, endDate) }
    }

    @Test
    fun `on execute throws when ApodThumbnailRepository throws`() {
        val startDate = LocalDate.of(1970, 1, 1)
        val endDate = LocalDate.of(1970, 2, 1)
        every {
            apodRepository.findByDateRange(startDate, endDate)
        } returns listOf(TestModels.apod())
        every {
            apodThumbnailRepository.findByDateRange(startDate, endDate)
        } throws RuntimeException("Test error 2")

        assertFailsWith<RuntimeException> { tested.execute(startDate, endDate) }
    }

    @Test
    fun `on execute throws when date range is longer than 100 days`() {
        val startDate = LocalDate.of(2020, 1, 1)
        // 2020-04-10, 101 days including both dates
        val endDate = startDate.plusDays(MAX_RANGE_DAYS.toLong())

        assertFailsWith<DateRangeTooLargeException> {
            tested.execute(startDate = startDate, endDate = endDate)
        }
        verify(exactly = 0) { apodRepository.findByDateRange(any(), any()) }
        verify(exactly = 0) {
            apodThumbnailRepository.findByDateRange(any(), any())
        }
    }

    @Test
    fun `on execute returns list of ApodWithThumbnail when date range is exactly 100 days`() {
        val startDate = LocalDate.of(2020, 1, 1)
        // 2020-04-09, 100 days including both dates
        val endDate = startDate.plusDays(MAX_RANGE_DAYS.toLong() - 1)
        val apod = TestModels.apod()
        every {
            apodRepository.findByDateRange(
                startDate = startDate,
                endDate = endDate,
            )
        } returns listOf(apod)
        every {
            apodThumbnailRepository.findByDateRange(
                startDate = startDate,
                endDate = endDate,
            )
        } returns emptyList()

        val result = tested.execute(startDate = startDate, endDate = endDate)

        assertEquals(listOf(TestModels.apodWithThumbnail(apod)), result)
    }

    @Test
    fun `on execute throws when start date is after end date`() {
        val startDate = LocalDate.of(2020, 1, 10)
        val endDate = LocalDate.of(2020, 1, 5)

        assertFailsWith<StartDateAfterEndDateException> {
            tested.execute(
                startDate = startDate,
                endDate = endDate,
            )
        }
        verify(exactly = 0) { apodRepository.findByDateRange(any(), any()) }
        verify(exactly = 0) {
            apodThumbnailRepository.findByDateRange(any(), any())
        }
    }

    @Test
    fun `on execute returns list of ApodWithThumbnail when start date is before end date`() {
        val startDate = LocalDate.of(1970, 1, 1)
        val endDate = LocalDate.of(1970, 2, 1)
        val apod1 = TestModels.apod(date = startDate, title = "apod1")
        val apod2 = TestModels.apod(date = endDate, title = "apod2")
        every {
            apodRepository.findByDateRange(startDate, endDate)
        } returns listOf(apod1, apod2)
        every {
            apodThumbnailRepository.findByDateRange(startDate, endDate)
        } returns emptyList()

        val result = tested.execute(startDate, endDate)

        assertEquals(
            listOf(
                TestModels.apodWithThumbnail(apod1),
                TestModels.apodWithThumbnail(apod2),
            ),
            result,
        )
    }

    @Test
    fun `on execute returns ApodWithThumbnail when start date is equal end date`() {
        val date = LocalDate.of(2025, 1, 1)
        val apod = TestModels.apod(date = date)
        every {
            apodRepository.findByDateRange(
                startDate = date,
                endDate = date,
            )
        } returns listOf(apod)
        every {
            apodThumbnailRepository.findByDateRange(
                startDate = date,
                endDate = date,
            )
        } returns emptyList()

        val result = tested.execute(startDate = date, endDate = date)

        assertEquals(listOf(TestModels.apodWithThumbnail(apod)), result)
    }

    @Test
    fun `on execute returns empty list when ApodRepository returns empty list`() {
        val startDate = LocalDate.of(2001, 1, 1)
        val endDate = LocalDate.of(2001, 1, 5)
        every {
            apodRepository.findByDateRange(startDate, endDate)
        } returns emptyList()
        every {
            apodThumbnailRepository.findByDateRange(startDate, endDate)
        } returns emptyList()

        val result = tested.execute(startDate, endDate)

        assertEquals(emptyList(), result)
    }

    @Test
    fun `on execute attaches the ApodThumbnail with the same date to each Apod and null to the others`() {
        val startDate = LocalDate.of(2020, 5, 1)
        val endDate = LocalDate.of(2020, 5, 3)
        val apod1 = TestModels.apod(date = LocalDate.of(2020, 5, 1))
        val apod2 = TestModels.apod(date = LocalDate.of(2020, 5, 2))
        val apod3 = TestModels.apod(date = LocalDate.of(2020, 5, 3))
        val thumbnail1 =
            TestModels.apodThumbnail(
                LocalDate.of(2020, 5, 1),
                Instant.parse("2026-11-20T08:00:00Z"),
            )
        val thumbnail3 =
            TestModels.apodThumbnail(
                LocalDate.of(2020, 5, 3),
                Instant.parse("2026-11-21T09:30:00Z"),
            )
        every {
            apodRepository.findByDateRange(startDate, endDate)
        } returns listOf(apod1, apod2, apod3)
        every {
            apodThumbnailRepository.findByDateRange(startDate, endDate)
        } returns listOf(thumbnail3, thumbnail1)

        val result = tested.execute(startDate, endDate)

        assertEquals(
            listOf(
                TestModels.apodWithThumbnail(apod1, thumbnail1),
                TestModels.apodWithThumbnail(apod2, null),
                TestModels.apodWithThumbnail(apod3, thumbnail3),
            ),
            result,
        )
    }

    @Test
    fun `on execute keeps the order of the Apods returned by ApodRepository`() {
        val startDate = LocalDate.of(2020, 6, 1)
        val endDate = LocalDate.of(2020, 6, 2)
        val apod1 = TestModels.apod(date = LocalDate.of(2020, 6, 1))
        val apod2 = TestModels.apod(date = LocalDate.of(2020, 6, 2))
        every {
            apodRepository.findByDateRange(startDate, endDate)
        } returns listOf(apod1, apod2)
        every {
            apodThumbnailRepository.findByDateRange(startDate, endDate)
        } returns
            listOf(
                TestModels.apodThumbnail(LocalDate.of(2020, 6, 2)),
                TestModels.apodThumbnail(LocalDate.of(2020, 6, 1)),
            )

        val result = tested.execute(startDate, endDate)

        assertEquals(listOf(apod1, apod2), result.map { it.apod })
    }

    @Test
    fun `on execute ignores an ApodThumbnail whose date has no Apod`() {
        val startDate = LocalDate.of(2020, 7, 1)
        val endDate = LocalDate.of(2020, 7, 2)
        val apod = TestModels.apod(date = LocalDate.of(2020, 7, 1))
        every {
            apodRepository.findByDateRange(startDate, endDate)
        } returns listOf(apod)
        every {
            apodThumbnailRepository.findByDateRange(startDate, endDate)
        } returns listOf(TestModels.apodThumbnail(LocalDate.of(2020, 7, 2)))

        val result = tested.execute(startDate, endDate)

        assertEquals(listOf(TestModels.apodWithThumbnail(apod, null)), result)
    }
}
