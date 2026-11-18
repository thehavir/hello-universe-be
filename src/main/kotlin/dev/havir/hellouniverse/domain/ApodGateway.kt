package dev.havir.hellouniverse.domain

import dev.havir.hellouniverse.domain.model.Apod
import java.time.LocalDate

interface ApodGateway {
    fun getApod(date: LocalDate): Apod?
}
