package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.exception.ApodNotFoundException
import java.time.Clock
import java.time.LocalDate

class FetchTodayApodUseCase(
    private val apodGateway: ApodGateway,
    private val apodRepository: ApodRepository,
    private val clock: Clock,
) {
    fun execute() {
        val today = LocalDate.now(clock)
        val apodExists = apodRepository.existsByDate(today)
        if (apodExists) {
            return
        }

        val apod =
            apodGateway.getApod() ?: throw ApodNotFoundException(
                "APOD not found for date: $today",
            )

        if (apod.date != today) {
            return
        }

        apodRepository.save(apod)
    }
}
