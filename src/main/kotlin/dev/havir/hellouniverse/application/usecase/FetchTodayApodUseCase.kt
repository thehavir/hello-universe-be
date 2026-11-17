package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.LocalDate

class FetchTodayApodUseCase(
    private val apodGateway: ApodGateway,
    private val apodRepository: ApodRepository,
    private val clock: Clock,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun execute() {
        val today = LocalDate.now(clock)
        if (apodRepository.existsByDate(today)) {
            logger.debug("APOD for {} is already stored", today)
            return
        }

        val apod = apodGateway.getApod(today)
        if (apod == null) {
            logger.info("APOD for {} is not published yet", today)
            return
        }

        apodRepository.save(apod)
        logger.info("Saved APOD for {}: {}", today, apod.title)
    }
}
