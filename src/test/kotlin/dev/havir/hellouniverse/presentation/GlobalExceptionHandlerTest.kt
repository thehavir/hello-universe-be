package dev.havir.hellouniverse.presentation

import dev.havir.hellouniverse.domain.exception.DateRangeTooLargeException
import dev.havir.hellouniverse.domain.exception.StartDateAfterEndDateException
import dev.havir.hellouniverse.presentation.response.ErrorResponse
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.core.MethodParameter
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@ExtendWith(OutputCaptureExtension::class)
class GlobalExceptionHandlerTest {
    lateinit var tested: GlobalExceptionHandler

    @BeforeEach
    fun setUp() {
        tested = GlobalExceptionHandler()
    }

    @Test
    fun `on handleStartAfterEnd returns ErrorResponse with BAD_REQUEST status`() {
        val expectedMessage = "test error 1"

        val result = tested.handleStartAfterEnd(
            StartDateAfterEndDateException(expectedMessage)
        )

        assertEquals(
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = expectedMessage,
                    timestamp = result.body!!.timestamp,
                )
            ), result
        )
    }

    @Test
    fun `on handleRangeTooLarge returns ErrorResponse with BAD_REQUEST status`() {
        val expectedMessage = "test error 2"

        val result = tested.handleRangeTooLarge(
            DateRangeTooLargeException(expectedMessage)
        )

        assertEquals(
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = expectedMessage,
                    timestamp = result.body!!.timestamp,
                )
            ), result
        )
    }

    @Test
    fun `on handleMissingParam returns ErrorResponse with BAD_REQUEST status`() {
        val expectedParameter = "start_date"

        val result = tested.handleMissingParam(
            MissingServletRequestParameterException(expectedParameter, "String")
        )

        assertEquals(
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = "$expectedParameter is required",
                    timestamp = result.body!!.timestamp,
                )
            ), result
        )
    }

    @Test
    fun `on handleTypeMismatch for a date returns ErrorResponse with format hint`() {
        val expectedParameter = "start_date"

        val result = tested.handleTypeMismatch(
            MethodArgumentTypeMismatchException(
                "01-01-2020",
                LocalDate::class.java,
                expectedParameter,
                mockk<MethodParameter>(relaxed = true),
                null,
            )
        )

        assertEquals(
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = "$expectedParameter must be in YYYY-MM-DD format",
                    timestamp = result.body!!.timestamp,
                )
            ), result
        )
    }

    @Test
    fun `on handleTypeMismatch for a non-date returns generic ErrorResponse`() {
        val expectedParameter = "limit"

        val result = tested.handleTypeMismatch(
            MethodArgumentTypeMismatchException(
                "abc",
                Int::class.java,
                expectedParameter,
                mockk<MethodParameter>(relaxed = true),
                null,
            )
        )

        assertEquals(
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    error = HttpStatus.BAD_REQUEST.reasonPhrase,
                    message = "$expectedParameter has an invalid value",
                    timestamp = result.body!!.timestamp,
                )
            ), result
        )
    }

    @Test
    fun `on handleNotFound returns ErrorResponse with NOT_FOUND status`() {
        val result = tested.handleNotFound(
            NoResourceFoundException(HttpMethod.GET, "/foo", "foo")
        )

        assertEquals(
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse(
                    status = HttpStatus.NOT_FOUND.value(),
                    error = HttpStatus.NOT_FOUND.reasonPhrase,
                    message = "No endpoint GET /foo",
                    timestamp = result.body!!.timestamp,
                )
            ), result
        )
    }

    @Test
    fun `on handleMethodNotAllowed returns ErrorResponse with METHOD_NOT_ALLOWED status`() {
        val result = tested.handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException("POST")
        )

        assertEquals(
            ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                ErrorResponse(
                    status = HttpStatus.METHOD_NOT_ALLOWED.value(),
                    error = HttpStatus.METHOD_NOT_ALLOWED.reasonPhrase,
                    message = "POST is not supported",
                    timestamp = result.body!!.timestamp,
                )
            ), result
        )
    }

    @Test
    fun `on handleUnexpected logs the error with its stack trace`(output: CapturedOutput) {
        tested.handleUnexpected(RuntimeException("some internal detail"))

        assertContains(output.out, "ERROR")
        assertContains(output.out, "Unexpected error")
        assertContains(
            output.out, "java.lang.RuntimeException: some internal detail"
        )
    }

    @Test
    fun `on handleUnexpected returns generic ErrorResponse with INTERNAL_SERVER_ERROR status`() {
        val result = tested.handleUnexpected(
            RuntimeException("some internal detail")
        )

        assertEquals(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse(
                    status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                    message = "Unexpected error",
                    timestamp = result.body!!.timestamp,
                )
            ), result
        )
    }

    @Test
    fun `on handleUnexpected keeps the status of Spring MVC exceptions`(output: CapturedOutput) {
        val result = tested.handleUnexpected(
            HttpMediaTypeNotSupportedException("text/plain is not supported")
        )

        assertEquals(
            ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                ErrorResponse(
                    status = HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                    error = HttpStatus.UNSUPPORTED_MEDIA_TYPE.reasonPhrase,
                    message = HttpStatus.UNSUPPORTED_MEDIA_TYPE.reasonPhrase,
                    timestamp = result.body!!.timestamp,
                )
            ), result
        )
        assertFalse(output.out.contains("Unexpected error"))
    }
}
