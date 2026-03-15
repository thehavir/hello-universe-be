package dev.havir.hellouniverse.domain

import dev.havir.hellouniverse.domain.model.Apod

interface ApodGateway {
    fun getApod(): Apod?
}
