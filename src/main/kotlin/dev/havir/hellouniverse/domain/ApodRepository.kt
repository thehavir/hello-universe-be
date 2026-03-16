package dev.havir.hellouniverse.domain

import dev.havir.hellouniverse.domain.model.Apod
import java.time.LocalDate

interface ApodRepository {
    fun save(apod: Apod): Apod

    fun findByDate(date: LocalDate): Apod?

    fun findByDateRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Apod>

    fun existsByDate(date: LocalDate): Boolean
}
