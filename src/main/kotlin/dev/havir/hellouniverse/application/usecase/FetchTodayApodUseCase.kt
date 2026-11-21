package dev.havir.hellouniverse.application.usecase

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.ApodThumbnailRepository
import dev.havir.hellouniverse.domain.ThumbnailGenerator
import dev.havir.hellouniverse.domain.exception.ThumbnailGenerationException
import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.ApodThumbnail
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Instant
import java.time.LocalDate

class FetchTodayApodUseCase(
    private val apodGateway: ApodGateway,
    private val apodRepository: ApodRepository,
    private val apodThumbnailRepository: ApodThumbnailRepository,
    private val thumbnailGenerator: ThumbnailGenerator,
    private val clock: Clock,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun execute() {
        val today = LocalDate.now(clock)
        val apod =
            apodRepository.findByDate(today) ?: fetchAndSave(today) ?: return
        if (apodThumbnailRepository.existsByDate(today)) return

        createThumbnail(apod)
    }

    private fun fetchAndSave(today: LocalDate): Apod? {
        val apod = apodGateway.getApod(today)
        if (apod == null) {
            logger.info("APOD for {} is not published yet", today)
            return null
        }

        apodRepository.save(apod)
        logger.info("Saved APOD for {}: {}", today, apod.title)
        return apod
    }

    private fun createThumbnail(apod: Apod) {
        try {
            val isThumbnailGenerated = thumbnailGenerator.generate(apod)
            if (isThumbnailGenerated) {
                apodThumbnailRepository.save(
                    ApodThumbnail(apod.date, Instant.now(clock)),
                )
                logger.info("Saved thumbnail for {}", apod.date)
            }
        } catch (e: ThumbnailGenerationException) {
            logger.warn(
                "Thumbnail for {} failed",
                apod.date,
                e,
            )
        }
    }
}
