package dev.havir.hellouniverse.presentation.controller

import dev.havir.hellouniverse.application.usecase.GetApodsByDateRangeUseCase
import dev.havir.hellouniverse.presentation.response.ApodResponse
import dev.havir.hellouniverse.presentation.response.toResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/apods")
class ApodController(private val getApodsByDateRangeUseCase: GetApodsByDateRangeUseCase) {

    @GetMapping
    fun getApods(
        @RequestParam("start_date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate,

        @RequestParam("end_date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate
    ): List<ApodResponse> = getApodsByDateRangeUseCase.execute(
        startDate = startDate, endDate = endDate
    ).toResponse()
}
