package dev.havir.hellouniverse.infrastructure.scheduler

import dev.havir.hellouniverse.application.usecase.FetchTodayApodUseCase
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class FetchTodayApodScheduler(private val fetchTodayApodUseCase: FetchTodayApodUseCase) {
    @Scheduled(cron = $$"${scheduler.fetch-apod.cron}")
    fun fetchApod() = fetchTodayApodUseCase.execute()
}
