package dev.havir.hellouniverse.infrastructure.persistence

import dev.havir.hellouniverse.infrastructure.persistence.entity.ApodThumbnailEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ApodThumbnailJpaRepository :
    JpaRepository<ApodThumbnailEntity, LocalDate> {
    fun findByDateBetween(
        from: LocalDate,
        to: LocalDate,
    ): List<ApodThumbnailEntity>
}
