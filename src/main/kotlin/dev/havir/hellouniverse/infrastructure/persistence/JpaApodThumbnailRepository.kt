package dev.havir.hellouniverse.infrastructure.persistence

import dev.havir.hellouniverse.domain.ApodThumbnailRepository
import dev.havir.hellouniverse.domain.model.ApodThumbnail
import dev.havir.hellouniverse.infrastructure.persistence.entity.ApodThumbnailEntity
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class JpaApodThumbnailRepository(
    private val apodThumbnailJpaRepository: ApodThumbnailJpaRepository,
) : ApodThumbnailRepository {
    override fun save(thumbnail: ApodThumbnail): ApodThumbnail =
        apodThumbnailJpaRepository.save(thumbnail.toEntity()).toDomain()

    override fun existsByDate(date: LocalDate): Boolean =
        apodThumbnailJpaRepository.existsById(date)

    override fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<ApodThumbnail> =
        apodThumbnailJpaRepository
            .findByDateBetween(from = startDate, to = endDate)
            .map { it.toDomain() }

    private fun ApodThumbnail.toEntity() =
        ApodThumbnailEntity(date = date, generatedAt = generatedAt)

    private fun ApodThumbnailEntity.toDomain() =
        ApodThumbnail(date = date, generatedAt = generatedAt)
}
