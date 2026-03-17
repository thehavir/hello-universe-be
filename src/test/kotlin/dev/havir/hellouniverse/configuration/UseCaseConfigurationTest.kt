package dev.havir.hellouniverse.configuration

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.ApodRepository
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertNotNull
import kotlin.test.Test

class UseCaseConfigurationTest {
    lateinit var apodGateway: ApodGateway
    lateinit var apodRepository: ApodRepository
    lateinit var tested: UseCaseConfiguration

    @BeforeEach
    fun setUp() {
        apodGateway = mockk()
        apodRepository = mockk()
        tested = UseCaseConfiguration(
            apodGateway = apodGateway, apodRepository = apodRepository
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
