package dev.havir.hellouniverse.infrastructure.persistence

import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType
import dev.havir.hellouniverse.infrastructure.persistence.entity.ApodEntity
import dev.havir.hellouniverse.infrastructure.persistence.entity.MediaTypeEntity
import org.springframework.stereotype.Repository
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

@Repository
class JpaApodRepository(
    private val apodJpaRepository: ApodJpaRepository,
) : ApodRepository {
    override fun save(apod: Apod): Apod =
        apodJpaRepository.save(apod.toEntity()).toDomain()

    override fun findByDate(date: LocalDate): Apod? =
        apodJpaRepository.findById(date).getOrNull()?.toDomain()

    override fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Apod> =
        apodJpaRepository
            .findByDateBetweenOrderByDateAsc(from = startDate, to = endDate)
            .map {
                it.toDomain()
            }

    override fun existsByDate(date: LocalDate): Boolean =
        apodJpaRepository.existsById(date)

    private fun Apod.toEntity(): ApodEntity =
        ApodEntity(
            date = date,
            title = title,
            explanation = explanation,
            credit = credit,
            copyright = copyright,
            mediaType = mediaType.toEntity(),
            url = url,
            sourceUrl = sourceUrl,
        )

    private fun ApodEntity.toDomain(): Apod =
        Apod(
            date = date,
            title = title,
            explanation = explanation,
            credit = credit,
            copyright = copyright,
            mediaType = mediaType.toDomain(),
            url = url,
            sourceUrl = sourceUrl,
        )

    private fun MediaTypeEntity.toDomain(): MediaType =
        when (this) {
            MediaTypeEntity.IMAGE -> MediaType.IMAGE
            MediaTypeEntity.VIDEO -> MediaType.VIDEO
            MediaTypeEntity.WEB_APP -> MediaType.WEB_APP
            MediaTypeEntity.OTHER -> MediaType.OTHER
        }

    private fun MediaType.toEntity(): MediaTypeEntity =
        when (this) {
            MediaType.IMAGE -> MediaTypeEntity.IMAGE
            MediaType.VIDEO -> MediaTypeEntity.VIDEO
            MediaType.WEB_APP -> MediaTypeEntity.WEB_APP
            MediaType.OTHER -> MediaTypeEntity.OTHER
        }
}
