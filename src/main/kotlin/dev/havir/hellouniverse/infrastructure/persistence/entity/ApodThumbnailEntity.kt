package dev.havir.hellouniverse.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate

@Entity
@Table(name = "apod_thumbnail")
class ApodThumbnailEntity(
    @Id
    @Column(nullable = false, unique = true)
    var date: LocalDate,
    @Column(name = "generated_at", nullable = false)
    var generatedAt: Instant,
)
