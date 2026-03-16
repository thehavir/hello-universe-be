package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.exception.ApodNotFoundException
import java.time.LocalDate

class FetchTodayApodUseCase(
    private val apodGateway: ApodGateway,
    private val apodRepository: ApodRepository
) {

    fun execute() {
        val now = LocalDate.now()
        val apodExists = apodRepository.existsByDate(now)
        if (apodExists) {
            return
        }

        val apod = apodGateway.getApod()
            ?: throw ApodNotFoundException("APOD not found for date: $now")

        apodRepository.save(apod)
    }
}
