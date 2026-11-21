package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.model.MediaType
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test

class FetchTodayApodUseCaseTest {
    val today: LocalDate = LocalDate.of(2000, 1, 1)
    val clock: Clock =
        Clock.fixed(
            Instant.parse("2000-01-01T12:00:00Z"),
            ZoneId.of("America/New_York"),
        )
    lateinit var apodGateway: ApodGateway
    lateinit var apodRepository: ApodRepository
    lateinit var tested: FetchTodayApodUseCase

    @BeforeEach
    fun setUp() {
        apodGateway = mockk()
        apodRepository = mockk()
        tested =
            FetchTodayApodUseCase(
                apodGateway = apodGateway,
                apodRepository = apodRepository,
                clock = clock,
            )
    }

    @Test
    fun `on execute calls existsByDate with today from clock`() {
        every { apodRepository.existsByDate(today) } returns true

        tested.execute()

        verify(exactly = 1) { apodRepository.existsByDate(today) }
    }

    @Test
    fun `on execute uses the clock zone date, not the UTC date`() {
        // 03:00 UTC on Jan 2 is still 22:00 on Jan 1 in New York.
        val nightClock =
            Clock.fixed(
                Instant.parse("2000-01-02T03:00:00Z"),
                ZoneId.of("America/New_York"),
            )
        tested = FetchTodayApodUseCase(apodGateway, apodRepository, nightClock)
        every { apodRepository.existsByDate(any()) } returns true

        tested.execute()

        verify(exactly = 1) {
            apodRepository.existsByDate(LocalDate.of(2000, 1, 1))
        }
    }

    @Test
    fun `on execute does not call getApod on ApodGateway when existsByDate on ApodRepository returns true for today`() {
        every { apodRepository.existsByDate(today) } returns true

        tested.execute()

        verify { apodGateway wasNot called }
        verify(exactly = 0) { apodRepository.save(any()) }
    }

    @Test
    fun `on execute calls getApod on ApodGateway when existsByDate on ApodRepository returns false for today`() {
        val apod = TestModels.apod(date = today)
        every { apodRepository.existsByDate(today) } returns false
        every { apodRepository.save(apod) } returns apod
        every { apodGateway.getApod(today) } returns apod

        tested.execute()

        verify { apodGateway.getApod(today) }
    }

    @Test
    fun `on execute saves Apod into the ApodRepository when ApodGateway returns Apod for today`() {
        val apod =
            TestModels.apod(
                today,
                "title-a",
                "explanation-b",
                MediaType.VIDEO,
                "url.com",
                null,
                "thumbnail.com",
                "xyz",
            )
        every { apodRepository.existsByDate(today) } returns false
        every { apodGateway.getApod(today) } returns apod
        every { apodRepository.save(apod) } returns apod

        tested.execute()

        verify { apodRepository.save(apod) }
    }

    @Test
    fun `on execute does not save when ApodGateway returns null`() {
        every { apodRepository.existsByDate(today) } returns false
        every { apodGateway.getApod(today) } returns null

        tested.execute()

        verify(exactly = 0) { apodRepository.save(any()) }
    }

    @Test
    fun `on execute re-throw when ApodGateway throws`() {
        every { apodRepository.existsByDate(today) } returns false
        every { apodGateway.getApod(today) } throws NullPointerException()

        assertThrows<NullPointerException> { tested.execute() }
        verify(exactly = 0) { apodRepository.save(any()) }
    }
}
