package dev.havir.hellouniverse.persistence

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.model.MediaType
import dev.havir.hellouniverse.infrastructure.persistence.ApodJpaRepository
import dev.havir.hellouniverse.infrastructure.persistence.JpaApodRepository
import dev.havir.hellouniverse.infrastructure.persistence.entity.MediaTypeEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNull
import java.time.LocalDate
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JpaApodRepositoryTest {
    lateinit var apodJpaRepository: ApodJpaRepository
    lateinit var tested: ApodRepository

    @BeforeEach
    fun setUp() {
        apodJpaRepository = mockk()
        tested = JpaApodRepository(apodJpaRepository)
    }

    @Test
    fun `on save calls save on ApodJpaRepository`() {
        val apodDomain = TestModels.apod()
        val apodEntity = TestModels.apodEntity()
        every { apodJpaRepository.save(any()) } returns apodEntity

        tested.save(apodDomain)

        verify { apodJpaRepository.save(any()) }
    }

    @Test
    fun `on save maps Apod to ApodEntity and save it and returns Apod`() {
        val apodDomain = TestModels.apod(
            LocalDate.of(1999, 2, 1),
            "title-a",
            "explanation-b",
            MediaType.VIDEO,
            "url.com",
            null,
            "thumbnail.com",
            "public",
            "xyz"
        )
        val apodEntity = TestModels.apodEntity(
            LocalDate.of(1999, 2, 1),
            "title-a",
            "explanation-b",
            MediaTypeEntity.VIDEO,
            "url.com",
            null,
            "thumbnail.com",
            "public",
            "xyz"
        )
        every { apodJpaRepository.save(any()) } returns apodEntity

        val result = tested.save(apodDomain)

        assertEquals(result, apodDomain)
    }

    @Test
    fun `on findByDate calls findById on ApodJpaRepository with passed date`() {
        val date = LocalDate.of(2014, 1, 12)
        val apodEntity = TestModels.apodEntity(date = date)
        every { apodJpaRepository.findById(date) } returns Optional.of(
            apodEntity
        )

        tested.findByDate(date)

        verify { apodJpaRepository.findById(date) }
    }

    @Test
    fun `on findByDate returns null when findById from ApodJpaRepository returns empty`() {
        val date = LocalDate.of(2014, 1, 13)
        every { apodJpaRepository.findById(date) } returns Optional.empty()

        val result = tested.findByDate(date)

        assertNull(result)
    }

    @Test
    fun `on findByDate returns Apod mapped from ApodEntity when findById on ApodJpaRepository returns ApodEntity`() {
        val date = LocalDate.of(2014, 1, 12)
        val apodDomain = TestModels.apod(
            date,
            "title-x",
            "explanation-y",
            MediaType.IMAGE,
            "url.de",
            "hd.com",
            null,
            null,
            "2"
        )
        val apodEntity = TestModels.apodEntity(
            date,
            "title-x",
            "explanation-y",
            MediaTypeEntity.IMAGE,
            "url.de",
            "hd.com",
            null,
            null,
            "2"
        )
        every { apodJpaRepository.findById(date) } returns Optional.of(
            apodEntity
        )

        val result = tested.findByDate(date)

        assertEquals(result, apodDomain)
    }

    @Test
    fun `on findByDateRange calls findByDateBetween on ApodJpaRepository`() {
        val startDate = LocalDate.of(2014, 1, 14)
        val endDate = LocalDate.of(2014, 1, 28)
        val apodEntities = listOf(
            TestModels.apodEntity(), TestModels.apodEntity()
        )
        every {
            apodJpaRepository.findByDateBetween(
                startDate, endDate
            )
        } returns apodEntities

        tested.findByDateRange(startDate, endDate)

        verify { apodJpaRepository.findByDateBetween(startDate, endDate) }
    }

    @Test
    fun `on findByDateRange returns empty list when findByDateBetween on ApodJpaRepository returns empty list`() {
        val startDate = LocalDate.of(2014, 1, 18)
        val endDate = LocalDate.of(2014, 1, 28)
        every {
            apodJpaRepository.findByDateBetween(
                startDate, endDate
            )
        } returns emptyList()

        val result = tested.findByDateRange(startDate, endDate)

        assertEquals(result, emptyList())
    }

    @Test
    fun `on findByDateRange returns Apod list mapped from ApodEntity list when findByDateBetween on ApodJpaRepository returns list of ApodEntity`() {
        val startDate = LocalDate.of(2014, 2, 1)
        val endDate = LocalDate.of(2014, 2, 3)
        val apodEntities = listOf(
            TestModels.apodEntity(
                startDate,
                "title-1",
                "explanation-1",
                MediaTypeEntity.IMAGE,
                "url.com",
                "hd.com",
                null,
                "CC2",
                "3-x"
            ), TestModels.apodEntity(
                endDate,
                "title-2",
                "explanation-2",
                MediaTypeEntity.OTHER,
                null,
                null,
                null,
                "CC-Y",
                "3-x"
            )
        )
        val apodDomains = listOf(
            TestModels.apod(
                startDate,
                "title-1",
                "explanation-1",
                MediaType.IMAGE,
                "url.com",
                "hd.com",
                null,
                "CC2",
                "3-x"
            ), TestModels.apod(
                endDate,
                "title-2",
                "explanation-2",
                MediaType.OTHER,
                null,
                null,
                null,
                "CC-Y",
                "3-x"
            )
        )
        every {
            apodJpaRepository.findByDateBetween(
                startDate, endDate
            )
        } returns apodEntities

        val result = tested.findByDateRange(startDate, endDate)

        assertEquals(result, apodDomains)
    }

    @Test
    fun `on existsByDate returns false when existsById on ApodJpaRepository returns false`() {
        val date = LocalDate.of(2020, 10, 8)
        every { apodJpaRepository.existsById(date) } returns false

        val result = tested.existsByDate(date)

        assertFalse { result }
    }

    @Test
    fun `on existsByDate returns true when existsById on ApodJpaRepository returns true`() {
        val date = LocalDate.of(2020, 11, 9)
        every { apodJpaRepository.existsById(date) } returns true

        val result = tested.existsByDate(date)

        assertTrue { result }
    }
}
