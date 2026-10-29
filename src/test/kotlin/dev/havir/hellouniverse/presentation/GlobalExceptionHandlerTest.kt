package dev.havir.hellouniverse.presentation

import dev.havir.hellouniverse.domain.exception.StartDateAfterEndDateException
import dev.havir.hellouniverse.presentation.response.ErrorResponse
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.HttpStatus
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalExceptionHandlerTest {
    lateinit var tested: GlobalExceptionHandler

    @BeforeEach
    fun setUp() {
        tested = GlobalExceptionHandler()
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Instant::class)
    }

    @Test
    fun `on handleBadRequestException returns ErrorResponse with BAD_REQUEST status`() {
        val now = Instant.parse("2000-12-06T12:33:00Z")
        mockkStatic(Instant::class)
        every { Instant.now() } returns now
        val expectedMessage = "test error 1"

        val result = tested.handleBadRequestException(
            StartDateAfterEndDateException(expectedMessage)
        )

        assertEquals(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = expectedMessage,
                timestamp = now
            ), result
        )
    }
}
