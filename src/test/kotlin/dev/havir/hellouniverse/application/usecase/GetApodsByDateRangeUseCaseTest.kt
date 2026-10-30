package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.application.usecase.GetApodsByDateRangeUseCase.Companion.MAX_RANGE_DAYS
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.exception.DateRangeTooLargeException
import dev.havir.hellouniverse.domain.exception.StartDateAfterEndDateException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GetApodsByDateRangeUseCaseTest {
    lateinit var apodRepository: ApodRepository
    lateinit var tested: GetApodsByDateRangeUseCase

    @BeforeEach
    fun setUp() {
        apodRepository = mockk()
        tested = GetApodsByDateRangeUseCase(apodRepository)
    }

    @Test
    fun `on execute calls findByDateRange on the ApodRepository`() {
        val startDate = LocalDate.of(1970, 1, 1)
        val endDate = LocalDate.of(1970, 2, 1)
        val expected = listOf(TestModels.apod())
        every {
            apodRepository.findByDateRange(
                startDate,
                endDate,
            )
        } returns expected

        tested.execute(startDate, endDate)

        verify { apodRepository.findByDateRange(startDate, endDate) }
    }

    @Test
    fun `on execute throws when ApodRepository throws`() {
        val startDate = LocalDate.of(1970, 1, 1)
        val endDate = LocalDate.of(1970, 2, 1)
        val error = "Test error 1"
        every {
            apodRepository.findByDateRange(
                startDate,
                endDate,
            )
        } throws RuntimeException(error)

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
    }

    @Test
    fun `on execute returns list of Apods when date range is exactly 100 days`() {
        val startDate = LocalDate.of(2020, 1, 1)
        // 2020-04-09, 100 days including both dates
        val endDate = startDate.plusDays(MAX_RANGE_DAYS.toLong() - 1)
        val expected = listOf(TestModels.apod())
        every {
            apodRepository.findByDateRange(
                startDate = startDate,
                endDate = endDate,
            )
        } returns expected

        val result = tested.execute(startDate = startDate, endDate = endDate)

        assertEquals(expected, result)
    }

    @Test
    fun `on execute throws when start date is after end date`() {
        val startDate = LocalDate.of(2020, 1, 10)
        val endDate = LocalDate.of(2020, 1, 5)
        every {
            apodRepository.findByDateRange(
                startDate = startDate,
                endDate = endDate,
            )
        }

        assertFailsWith<StartDateAfterEndDateException> {
            tested.execute(
                startDate = startDate,
                endDate = endDate,
            )
        }
    }

    @Test
    fun `on execute returns list of Apods when start date is before end date`() {
        val startDate = LocalDate.of(1970, 1, 1)
        val endDate = LocalDate.of(1970, 2, 1)
        val expected =
            listOf(
                TestModels.apod(title = "apod1"),
                TestModels.apod(title = "apod2"),
            )
        every {
            apodRepository.findByDateRange(
                startDate,
                endDate,
            )
        } returns expected

        val result = tested.execute(startDate, endDate)

        assertEquals(expected, result)
    }

    @Test
    fun `on execute returns Apod when start date is equal end date`() {
        val expected = listOf(TestModels.apod())
        val date = LocalDate.of(2025, 1, 1)
        every {
            apodRepository.findByDateRange(
                startDate = date,
                endDate = date,
            )
        } returns expected

        val result = tested.execute(startDate = date, endDate = date)

        assertEquals(expected, result)
    }
}
