package dev.havir.hellouniverse.infrastructure.scheduler

import dev.havir.hellouniverse.application.usecase.FetchTodayApodUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertFailsWith

class FetchTodayApodSchedulerTest {
    lateinit var fetchTodayApodUseCase: FetchTodayApodUseCase
    lateinit var sut: FetchTodayApodScheduler

    @BeforeEach
    fun setUp() {
        fetchTodayApodUseCase = mockk()
        sut = FetchTodayApodScheduler(fetchTodayApodUseCase)
    }

    @Test
    fun `on scheduleFetchApod calls execute on FetchTodayApodUseCase`() {
        every { fetchTodayApodUseCase.execute() } answers { }

        sut.fetchApod()

        verify(exactly = 1) { fetchTodayApodUseCase.execute() }
    }

    @Test
    fun `on scheduleFetchApod throws when FetchTodayApodUseCase throws`() {
        every { fetchTodayApodUseCase.execute() } throws Exception("error55")

        assertFailsWith<Exception> { sut.fetchApod() }
    }
}
