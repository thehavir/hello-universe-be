package dev.havir.hellouniverse.domain

import dev.havir.hellouniverse.domain.model.ApodThumbnail
import java.time.LocalDate

interface ApodThumbnailRepository {
    fun save(thumbnail: ApodThumbnail): ApodThumbnail

    fun existsByDate(date: LocalDate): Boolean

    fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<ApodThumbnail>
}
