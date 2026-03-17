package dev.havir.hellouniverse.presentation.controller

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.application.usecase.GetApodsByDateRangeUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ApodControllerTest {
    lateinit var getApodsByDateRangeUseCase: GetApodsByDateRangeUseCase
    lateinit var tested: ApodController

    @BeforeEach
    fun setUp() {
        getApodsByDateRangeUseCase = mockk()
        tested = ApodController(getApodsByDateRangeUseCase)
    }

    @Test
    fun `on getApods calls execute on GetApodsUseCase with startDate and endDate`() {
        val apods = listOf(TestModels.apod(), TestModels.apod())
        val startDate = LocalDate.of(2019, 8, 8)
        val endDate = LocalDate.of(2020, 8, 8)
        every {
            getApodsByDateRangeUseCase.execute(
                startDate, endDate
            )
        } returns apods

        tested.getApods(startDate, endDate)

        verify { getApodsByDateRangeUseCase.execute(startDate, endDate) }
    }

    @Test
    fun `on getApods returns empty list when GetApodsUseCase returns empty list`() {
        val startDate = LocalDate.of(2014, 2, 1)
        val endDate = LocalDate.of(2014, 3, 1)
        every {
            getApodsByDateRangeUseCase.execute(
                startDate, endDate
            )
        } returns emptyList()

        val result = tested.getApods(startDate, endDate)

        assertEquals(result, emptyList())
    }

    @Test
    fun `on getApods returns list of ApodResponse when GetApodsUseCase returns list of Apods`() {
        val startDate = LocalDate.of(2014, 3, 10)
        val endDate = LocalDate.of(2014, 4, 10)
        val apods = listOf(
            TestModels.apod(title = "a1"), TestModels.apod(title = "a2")
        )
        every {
            getApodsByDateRangeUseCase.execute(
                startDate, endDate
            )
        } returns apods

        val result = tested.getApods(startDate, endDate)

        assertEquals(
            result, listOf(
                TestModels.apodResponse(title = "a1"),
                TestModels.apodResponse(title = "a2")
            )
        )
    }
}
