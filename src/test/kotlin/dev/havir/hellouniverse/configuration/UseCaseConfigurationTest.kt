package dev.havir.hellouniverse.configuration

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import dev.havir.hellouniverse.domain.ApodThumbnailRepository
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import java.time.Clock
import kotlin.test.Test

class UseCaseConfigurationTest {
    lateinit var apodGateway: ApodGateway
    lateinit var apodRepository: ApodRepository
    lateinit var apodThumbnailRepository: ApodThumbnailRepository
    lateinit var tested: UseCaseConfiguration

    @BeforeEach
    fun setUp() {
        apodGateway = mockk()
        apodRepository = mockk()
        apodThumbnailRepository = mockk()
        tested =
            UseCaseConfiguration(
                apodGateway = apodGateway,
                apodRepository = apodRepository,
                apodThumbnailRepository = apodThumbnailRepository,
                clock = Clock.systemUTC(),
            )
    }

    @Test
    fun `on fetchTodayApodUseCase returns FetchTodayApodUseCase`() {
        val result = tested.fetchTodayApodUseCase()

        assertNotNull(result)
    }

    @Test
    fun `on getApodsByDateRangeUseCase returns GetApodsByDateRangeUseCase`() {
        val result = tested.getApodsByDateRangeUseCase()

        assertNotNull(result)
    }
}
