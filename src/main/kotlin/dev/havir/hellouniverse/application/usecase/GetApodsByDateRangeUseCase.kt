package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.exception.StartDateAfterEndDateException
import dev.havir.hellouniverse.domain.model.Apod
import java.time.LocalDate

class GetApodsByDateRangeUseCase(private val apodRepository: ApodRepository) {
    fun execute(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<Apod> {
        if (startDate > endDate) {
            throw StartDateAfterEndDateException("start date cannot be after end date")
        }

        return apodRepository.findByDateRange(startDate, endDate)
    }
}
