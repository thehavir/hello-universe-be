package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.ApodThumbnailRepository
import dev.havir.hellouniverse.domain.ThumbnailGenerator
import dev.havir.hellouniverse.domain.exception.ThumbnailGenerationException
import dev.havir.hellouniverse.domain.model.MediaType
import io.mockk.called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchTodayApodUseCaseTest {
    val today: LocalDate = LocalDate.of(2000, 1, 1)
    val now: Instant = Instant.parse("2000-01-01T12:00:00Z")
    val clock: Clock = Clock.fixed(now, ZoneId.of("America/New_York"))
    lateinit var apodGateway: ApodGateway
    lateinit var apodRepository: ApodRepository
    lateinit var apodThumbnailRepository: ApodThumbnailRepository
    lateinit var thumbnailGenerator: ThumbnailGenerator
    lateinit var tested: FetchTodayApodUseCase

    @BeforeEach
    fun setUp() {
        apodGateway = mockk()
        apodRepository = mockk()
        apodThumbnailRepository = mockk()
        thumbnailGenerator = mockk()
        tested =
            FetchTodayApodUseCase(
                apodGateway = apodGateway,
                apodRepository = apodRepository,
                apodThumbnailRepository = apodThumbnailRepository,
                thumbnailGenerator = thumbnailGenerator,
                clock = clock,
            )
    }

    // --- today's date

    @Test
    fun `on execute calls findByDate with today from clock`() {
        every { apodRepository.findByDate(today) } returns TestModels.apod(date = today)
        every { apodThumbnailRepository.existsByDate(today) } returns true

        tested.execute()

        verify(exactly = 1) { apodRepository.findByDate(today) }
    }

    @Test
    fun `on execute uses the clock zone date, not the UTC date`() {
        // 03:00 UTC on Jan 2 is still 22:00 on Jan 1 in New York.
        val nightClock =
            Clock.fixed(
                Instant.parse("2000-01-02T03:00:00Z"),
                ZoneId.of("America/New_York"),
            )
        tested =
            FetchTodayApodUseCase(
                apodGateway,
                apodRepository,
                apodThumbnailRepository,
                thumbnailGenerator,
                nightClock,
            )
        every { apodRepository.findByDate(any()) } returns TestModels.apod()
        every { apodThumbnailRepository.existsByDate(any()) } returns true

        tested.execute()

        verify(exactly = 1) {
            apodRepository.findByDate(LocalDate.of(2000, 1, 1))
        }
    }

    // --- APOD already stored

    @Test
    fun `on execute does not call ApodGateway when the Apod for today is already stored`() {
        every { apodRepository.findByDate(today) } returns TestModels.apod(date = today)
        every { apodThumbnailRepository.existsByDate(today) } returns true

        tested.execute()

        verify { apodGateway wasNot called }
        verify(exactly = 0) { apodRepository.save(any()) }
    }

    @Test
    fun `on execute does nothing more when the Apod and its thumbnail are already stored`() {
        every { apodRepository.findByDate(today) } returns TestModels.apod(date = today)
        every { apodThumbnailRepository.existsByDate(today) } returns true

        tested.execute()

        verify { thumbnailGenerator wasNot called }
        verify(exactly = 0) { apodThumbnailRepository.save(any()) }
    }

    @Test
    fun `on execute makes the thumbnail when the Apod is stored but its thumbnail is not`() {
        val apod = TestModels.apod(date = today)
        every { apodRepository.findByDate(today) } returns apod
        every { apodThumbnailRepository.existsByDate(today) } returns false
        every { thumbnailGenerator.generate(apod) } returns true
        every { apodThumbnailRepository.save(any()) } answers { firstArg() }

        tested.execute()

        verify { thumbnailGenerator.generate(apod) }
        verify { apodThumbnailRepository.save(TestModels.apodThumbnail(today, now)) }
        verify { apodGateway wasNot called }
    }

    // --- fetching and saving

    @Test
    fun `on execute calls getApod on ApodGateway when the Apod for today is not stored`() {
        val apod = TestModels.apod(date = today)
        givenApodIsFetched(apod)
        every { thumbnailGenerator.generate(apod) } returns false

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
                "credit",
                null,
                "url.com",
                "xyz",
            )
        givenApodIsFetched(apod)
        every { thumbnailGenerator.generate(apod) } returns false

        tested.execute()

        verify { apodRepository.save(apod) }
    }

    @Test
    fun `on execute does nothing more when ApodGateway returns null`() {
        every { apodRepository.findByDate(today) } returns null
        every { apodGateway.getApod(today) } returns null

        tested.execute()

        verify(exactly = 0) { apodRepository.save(any()) }
        verify { thumbnailGenerator wasNot called }
        verify { apodThumbnailRepository wasNot called }
    }

    @Test
    fun `on execute re-throw when ApodGateway throws`() {
        every { apodRepository.findByDate(today) } returns null
        every { apodGateway.getApod(today) } throws NullPointerException()

        assertThrows<NullPointerException> { tested.execute() }
        verify(exactly = 0) { apodRepository.save(any()) }
        verify { thumbnailGenerator wasNot called }
    }

    @Test
    fun `on execute does not make a thumbnail and re-throws when saving the Apod fails`() {
        val apod = TestModels.apod(date = today)
        every { apodRepository.findByDate(today) } returns null
        every { apodGateway.getApod(today) } returns apod
        every { apodRepository.save(apod) } throws IllegalStateException()

        assertThrows<IllegalStateException> { tested.execute() }
        verify { thumbnailGenerator wasNot called }
    }

    // --- thumbnail of a new Apod

    @Test
    fun `on execute makes the thumbnail of a new Apod after saving it`() {
        val apod = TestModels.apod(date = today)
        givenApodIsFetched(apod)
        every { thumbnailGenerator.generate(apod) } returns true
        every { apodThumbnailRepository.save(any()) } answers { firstArg() }

        tested.execute()

        verifyOrder {
            apodRepository.save(apod)
            thumbnailGenerator.generate(apod)
            apodThumbnailRepository.save(TestModels.apodThumbnail(today, now))
        }
    }

    @Test
    fun `on execute saves the thumbnail with the date of the Apod and the time from clock`() {
        val apod = TestModels.apod(date = today)
        givenApodIsFetched(apod)
        every { thumbnailGenerator.generate(apod) } returns true
        every { apodThumbnailRepository.save(any()) } answers { firstArg() }

        tested.execute()

        verify {
            apodThumbnailRepository.save(
                match { it.date == today && it.generatedAt == now },
            )
        }
    }

    // --- thumbnail problems

    @Test
    fun `on execute does not save a thumbnail when the generator returns false`() {
        val apod = TestModels.apod(date = today, mediaType = MediaType.WEB_APP)
        givenApodIsFetched(apod)
        every { thumbnailGenerator.generate(apod) } returns false

        tested.execute()

        verify { apodRepository.save(apod) }
        verify(exactly = 0) { apodThumbnailRepository.save(any()) }
    }

    @Test
    fun `on execute keeps the saved Apod and does not throw when the generator throws ThumbnailGenerationException`() {
        val apod = TestModels.apod(date = today)
        givenApodIsFetched(apod)
        every { thumbnailGenerator.generate(apod) } throws
            ThumbnailGenerationException("download failed")

        tested.execute()

        verify { apodRepository.save(apod) }
        verify(exactly = 0) { apodThumbnailRepository.save(any()) }
    }

    @Test
    fun `on execute re-throws other exceptions from the generator`() {
        val apod = TestModels.apod(date = today)
        givenApodIsFetched(apod)
        every { thumbnailGenerator.generate(apod) } throws NullPointerException()

        assertThrows<NullPointerException> { tested.execute() }
        verify { apodRepository.save(apod) }
        verify(exactly = 0) { apodThumbnailRepository.save(any()) }
    }

    @Test
    fun `on execute re-throws when saving the thumbnail fails`() {
        val apod = TestModels.apod(date = today)
        givenApodIsFetched(apod)
        every { thumbnailGenerator.generate(apod) } returns true
        every { apodThumbnailRepository.save(any()) } throws IllegalStateException("db down")

        val error = assertThrows<IllegalStateException> { tested.execute() }

        assertEquals("db down", error.message)
    }

    private fun givenApodIsFetched(apod: dev.havir.hellouniverse.domain.model.Apod) {
        every { apodRepository.findByDate(apod.date) } returns null
        every { apodGateway.getApod(apod.date) } returns apod
        every { apodRepository.save(apod) } returns apod
        every { apodThumbnailRepository.existsByDate(apod.date) } returns false
    }
}
