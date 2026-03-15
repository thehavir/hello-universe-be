package dev.havir.hellouniverse.persistence

import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.infrastructure.persistence.ApodJpaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@DataJpaTest
class ApodJapRepositoryTest {
    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var sut: ApodJpaRepository

    @Test
    fun `on findByDateBetween returns list of ApodEntity between passed date range`() {
        val startDate = LocalDate.of(2020, 1, 5)
        val endDate = LocalDate.of(2020, 1, 6)
        val apod0 = TestModels.apodEntity(date = LocalDate.of(2020, 1, 4))
        val apod1 = TestModels.apodEntity(date = LocalDate.of(2020, 1, 5))
        val apod2 = TestModels.apodEntity(date = LocalDate.of(2020, 1, 6))
        val apod3 = TestModels.apodEntity(date = LocalDate.of(2020, 1, 7))
        val expectedResult = listOf(apod1, apod2)
        entityManager.persist(apod0)
        entityManager.persist(apod1)
        entityManager.persist(apod2)
        entityManager.persist(apod3)
        entityManager.flush()

        val result = sut.findByDateBetween(from = startDate, to = endDate)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `on findByDateBetween returns empty list when there is no ApodEntity between passed date range`() {
        val startDate = LocalDate.of(2001, 1, 5)
        val endDate = LocalDate.of(2001, 1, 10)
        val apod0 = TestModels.apodEntity(date = LocalDate.of(2001, 1, 4))
        val apod1 = TestModels.apodEntity(date = LocalDate.of(2001, 1, 11))
        entityManager.persist(apod0)
        entityManager.persist(apod1)
        entityManager.flush()

        val result = sut.findByDateBetween(from = startDate, to = endDate)

        assertEquals(emptyList(), result)
    }
}
