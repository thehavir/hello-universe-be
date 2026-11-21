package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.ApodThumbnailRepository
import dev.havir.hellouniverse.domain.exception.DateRangeTooLargeException
import dev.havir.hellouniverse.domain.exception.StartDateAfterEndDateException
import dev.havir.hellouniverse.domain.model.ApodWithThumbnail
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class GetApodsByDateRangeUseCase(
    private val apodRepository: ApodRepository,
    private val apodThumbnailRepository: ApodThumbnailRepository,
) {
    fun execute(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<ApodWithThumbnail> {
        if (startDate > endDate) {
            throw StartDateAfterEndDateException(
                "start_date cannot be after end_date",
            )
        }

        val days = ChronoUnit.DAYS.between(startDate, endDate) + 1
        if (days > MAX_RANGE_DAYS) {
            throw DateRangeTooLargeException(
                "date range cannot be longer than $MAX_RANGE_DAYS days",
            )
        }

        val apods = apodRepository.findByDateRange(startDate, endDate)
        val thumbnails =
            apodThumbnailRepository
                .findByDateRange(startDate, endDate)
                .associateBy { it.date }

        return apods.map { ApodWithThumbnail(it, thumbnails[it.date]) }
    }

    companion object {
        const val MAX_RANGE_DAYS = 100
    }
}
