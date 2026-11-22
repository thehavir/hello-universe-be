package dev.havir.hellouniverse.infrastructure.persistence

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.ApodThumbnailRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JpaApodThumbnailRepositoryTest {
    lateinit var apodThumbnailJpaRepository: ApodThumbnailJpaRepository
    lateinit var tested: ApodThumbnailRepository

    @BeforeEach
    fun setUp() {
        apodThumbnailJpaRepository = mockk()
        tested = JpaApodThumbnailRepository(apodThumbnailJpaRepository)
    }

    @Test
    fun `on save calls save on ApodThumbnailJpaRepository`() {
        val thumbnailDomain = TestModels.apodThumbnail()
        val thumbnailEntity = TestModels.apodThumbnailEntity()
        every { apodThumbnailJpaRepository.save(any()) } returns thumbnailEntity

        tested.save(thumbnailDomain)

        verify { apodThumbnailJpaRepository.save(any()) }
    }

    @Test
    fun `on save maps ApodThumbnail to ApodThumbnailEntity and save it and returns ApodThumbnail`() {
        val date = LocalDate.of(1999, 2, 1)
        val generatedAt = Instant.parse("2026-11-21T10:15:30Z")
        val thumbnailDomain = TestModels.apodThumbnail(date, generatedAt)
        val thumbnailEntity = TestModels.apodThumbnailEntity(date, generatedAt)
        every { apodThumbnailJpaRepository.save(any()) } returns thumbnailEntity

        val result = tested.save(thumbnailDomain)

        assertEquals(thumbnailDomain, result)
    }

    @Test
    fun `on existsByDate returns false when existsById on ApodThumbnailJpaRepository returns false`() {
        val date = LocalDate.of(2020, 10, 8)
        every { apodThumbnailJpaRepository.existsById(date) } returns false

        val result = tested.existsByDate(date)

        assertFalse { result }
    }

    @Test
    fun `on existsByDate returns true when existsById on ApodThumbnailJpaRepository returns true`() {
        val date = LocalDate.of(2020, 11, 9)
        every { apodThumbnailJpaRepository.existsById(date) } returns true

        val result = tested.existsByDate(date)

        assertTrue { result }
    }

    @Test
    fun `on findByDateRange calls findByDateBetween on ApodThumbnailJpaRepository`() {
        val startDate = LocalDate.of(2014, 1, 14)
        val endDate = LocalDate.of(2014, 1, 28)
        every {
            apodThumbnailJpaRepository.findByDateBetween(startDate, endDate)
        } returns listOf(TestModels.apodThumbnailEntity())

        tested.findByDateRange(startDate, endDate)

        verify {
            apodThumbnailJpaRepository.findByDateBetween(startDate, endDate)
        }
    }

    @Test
    fun `on findByDateRange returns empty list when findByDateBetween on ApodThumbnailJpaRepository returns empty list`() {
        val startDate = LocalDate.of(2014, 1, 18)
        val endDate = LocalDate.of(2014, 1, 28)
        every {
            apodThumbnailJpaRepository.findByDateBetween(startDate, endDate)
        } returns emptyList()

        val result = tested.findByDateRange(startDate, endDate)

        assertEquals(emptyList(), result)
    }

    @Test
    fun `on findByDateRange returns ApodThumbnail list mapped from ApodThumbnailEntity list when findByDateBetween on ApodThumbnailJpaRepository returns list of ApodThumbnailEntity`() {
        val startDate = LocalDate.of(2014, 2, 1)
        val endDate = LocalDate.of(2014, 2, 3)
        val firstGeneratedAt = Instant.parse("2026-11-20T08:00:00Z")
        val secondGeneratedAt = Instant.parse("2026-11-21T09:30:00Z")
        every {
            apodThumbnailJpaRepository.findByDateBetween(startDate, endDate)
        } returns
            listOf(
                TestModels.apodThumbnailEntity(startDate, firstGeneratedAt),
                TestModels.apodThumbnailEntity(endDate, secondGeneratedAt),
            )

        val result = tested.findByDateRange(startDate, endDate)

        assertEquals(
            listOf(
                TestModels.apodThumbnail(startDate, firstGeneratedAt),
                TestModels.apodThumbnail(endDate, secondGeneratedAt),
            ),
            result,
        )
    }
}
