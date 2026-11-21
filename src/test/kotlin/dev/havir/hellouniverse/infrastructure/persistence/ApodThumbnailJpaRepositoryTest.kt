package dev.havir.hellouniverse.infrastructure.persistence

import dev.havir.hellouniverse.TestModels
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

@DataJpaTest
@ActiveProfiles("dev")
class ApodThumbnailJpaRepositoryTest {
    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var tested: ApodThumbnailJpaRepository

    @Test
    fun `on findByDateBetween returns list of ApodThumbnailEntity between passed date range including both ends`() {
        val startDate = LocalDate.of(2020, 1, 5)
        val endDate = LocalDate.of(2020, 1, 6)
        val thumbnail0 =
            TestModels.apodThumbnailEntity(date = LocalDate.of(2020, 1, 4))
        val thumbnail1 =
            TestModels.apodThumbnailEntity(date = LocalDate.of(2020, 1, 5))
        val thumbnail2 =
            TestModels.apodThumbnailEntity(date = LocalDate.of(2020, 1, 6))
        val thumbnail3 =
            TestModels.apodThumbnailEntity(date = LocalDate.of(2020, 1, 7))
        entityManager.persist(thumbnail3)
        entityManager.persist(thumbnail2)
        entityManager.persist(thumbnail0)
        entityManager.persist(thumbnail1)
        entityManager.flush()

        val result = tested.findByDateBetween(from = startDate, to = endDate)

        assertEquals(
            setOf(thumbnail1, thumbnail2),
            result.toSet(),
        )
    }

    @Test
    fun `on findByDateBetween returns empty list when there is no ApodThumbnailEntity between passed date range`() {
        val startDate = LocalDate.of(2001, 1, 5)
        val endDate = LocalDate.of(2001, 1, 10)
        entityManager.persist(
            TestModels.apodThumbnailEntity(date = LocalDate.of(2001, 1, 4)),
        )
        entityManager.persist(
            TestModels.apodThumbnailEntity(date = LocalDate.of(2001, 1, 11)),
        )
        entityManager.flush()

        val result = tested.findByDateBetween(from = startDate, to = endDate)

        assertEquals(emptyList(), result)
    }
}
