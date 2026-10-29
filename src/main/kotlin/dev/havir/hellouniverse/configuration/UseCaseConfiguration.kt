package dev.havir.hellouniverse.configuration

import dev.havir.hellouniverse.application.usecase.FetchTodayApodUseCase
import dev.havir.hellouniverse.application.usecase.GetApodsByDateRangeUseCase
import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class UseCaseConfiguration(
    private val apodGateway: ApodGateway,
    private val apodRepository: ApodRepository,
    private val clock: Clock,
) {
    @Bean
    fun fetchTodayApodUseCase() =
        FetchTodayApodUseCase(apodGateway, apodRepository, clock)

    @Bean
    fun getApodsByDateRangeUseCase() =
        GetApodsByDateRangeUseCase(apodRepository)
}
