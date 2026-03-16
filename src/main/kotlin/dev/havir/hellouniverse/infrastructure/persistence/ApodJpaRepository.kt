package dev.havir.hellouniverse.infrastructure.persistence

import dev.havir.hellouniverse.infrastructure.persistence.entity.ApodEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface ApodJpaRepository : JpaRepository<ApodEntity, LocalDate> {
    fun findByDateBetween(from: LocalDate, to: LocalDate): List<ApodEntity>
}
