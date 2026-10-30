package dev.havir.hellouniverse.presentation

import dev.havir.hellouniverse.domain.exception.DateRangeTooLargeException
import dev.havir.hellouniverse.domain.exception.StartDateAfterEndDateException
import dev.havir.hellouniverse.presentation.response.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import java.time.LocalDate
import org.springframework.web.ErrorResponse as SpringErrorResponse

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(StartDateAfterEndDateException::class)
    fun handleStartAfterEnd(exception: StartDateAfterEndDateException) = error(
        HttpStatus.BAD_REQUEST,
        exception.message ?: "start_date cannot be after end_date",
    )

    @ExceptionHandler(DateRangeTooLargeException::class)
    fun handleRangeTooLarge(exception: DateRangeTooLargeException) = error(
        HttpStatus.BAD_REQUEST,
        exception.message ?: "date range is too large",
    )

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(exception: MissingServletRequestParameterException) =
        error(
            HttpStatus.BAD_REQUEST, "${exception.parameterName} is required"
        )

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(exception: MethodArgumentTypeMismatchException) =
        error(
            HttpStatus.BAD_REQUEST,
            if (exception.requiredType == LocalDate::class.java) {
                "${exception.name} must be in YYYY-MM-DD format"
            } else {
                "${exception.name} has an invalid value"
            }
        )

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNotFound(exception: NoResourceFoundException) = error(
        HttpStatus.NOT_FOUND,
        "No endpoint ${exception.httpMethod} /${exception.resourcePath}"
    )

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(exception: HttpRequestMethodNotSupportedException) =
        error(
            HttpStatus.METHOD_NOT_ALLOWED,
            "${exception.method} is not supported"
        )

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(exception: Exception): ResponseEntity<ErrorResponse> {
        // Other Spring MVC exceptions (406, 415, ...) carry their own status
        if (exception is SpringErrorResponse) {
            val status = HttpStatus.valueOf(exception.statusCode.value())
            return error(status, status.reasonPhrase)
        }

        logger.error("Unexpected error", exception)
        return error(
            HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error"
        )
    }

    private fun error(
        status: HttpStatus, message: String
    ): ResponseEntity<ErrorResponse> = ResponseEntity.status(status).body(
        ErrorResponse(
            status = status.value(),
            error = status.reasonPhrase,
            message = message,
        )
    )
}
