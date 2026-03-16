package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.exception.ApodNotFoundException
import dev.havir.hellouniverse.domain.model.MediaType
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.Test

class FetchTodayApodUseCaseTest {
    lateinit var apodGateway: ApodGateway
    lateinit var apodRepository: ApodRepository
    lateinit var sut: FetchTodayApodUseCase

    @BeforeEach
    fun setUp() {
        apodGateway = mockk()
        apodRepository = mockk()
        sut = FetchTodayApodUseCase(
            apodGateway = apodGateway, apodRepository = apodRepository
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(LocalDate::class)
    }

    @Test
    fun `on execute calls existsByDate today Apod`() {
        val now = LocalDate.of(2000, 1, 1)
        mockkStatic(LocalDate::class)
        every { LocalDate.now() } returns now
        every { apodRepository.existsByDate(now) } returns true

        sut.execute()

        verify(exactly = 1) { apodRepository.existsByDate(now) }
    }

    @Test
    fun `on execute does not call getApod on NasaApiClient when existsByDate on ApodRepository returns true for LocalDate now`() {
        val now = LocalDate.of(2000, 1, 1)
        mockkStatic(LocalDate::class)
        every { LocalDate.now() } returns now
        every { apodRepository.existsByDate(now) } returns true

        sut.execute()

        verify { apodGateway wasNot called }
        verify(exactly = 0) { apodRepository.save(any()) }
    }

    @Test
    fun `on execute calls getApod on the NasaApiClient when existsByDate  on ApodRepository returns false for LocalDate now`() {
        val now = LocalDate.of(2000, 1, 1)
        val apod = TestModels.apod()
        mockkStatic(LocalDate::class)
        every { LocalDate.now() } returns now
        every { apodRepository.existsByDate(now) } returns false
        every { apodRepository.save(apod) } returns apod
        every { apodGateway.getApod() } returns apod

        sut.execute()

        verify { apodGateway.getApod() }
    }

    @Test
    fun `on execute saves Apod into the ApodRepository when NasaApiClient returns ApodDto`() {
        val apod = TestModels.apod(
            LocalDate.of(2000, 1, 1),
            "title-a",
            "explanation-b",
            MediaType.VIDEO,
            "url.com",
            null,
            "thumbnail.com",
            "public",
            "xyz"
        )
        val now = LocalDate.of(2000, 1, 1)
        mockkStatic(LocalDate::class)
        every { LocalDate.now() } returns now
        every { apodRepository.existsByDate(now) } returns false
        every { apodGateway.getApod() } returns apod
        every { apodRepository.save(apod) } returns apod

        sut.execute()

        verify { apodRepository.save(apod) }
    }

    @Test
    fun `on execute throw ApodNotFoundException when nasaApiClient returns null`() {
        val now = LocalDate.of(2000, 1, 1)
        mockkStatic(LocalDate::class)
        every { LocalDate.now() } returns now
        every { apodRepository.existsByDate(now) } returns false
        every { apodGateway.getApod() } returns null

        assertThrows<ApodNotFoundException> { sut.execute() }
        verify(exactly = 0) { apodRepository.save(any()) }
    }

    @Test
    fun `on execute re-throw when nasaApiClient throws`() {
        val now = LocalDate.of(2000, 1, 1)
        mockkStatic(LocalDate::class)
        every { LocalDate.now() } returns now
        every { apodRepository.existsByDate(now) } returns false
        every { apodGateway.getApod() } throws NullPointerException()

        assertThrows<NullPointerException> { sut.execute() }
        verify(exactly = 0) { apodRepository.save(any()) }
    }
}
