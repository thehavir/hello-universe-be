package dev.havir.hellouniverse

import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType
import dev.havir.hellouniverse.infrastructure.persistence.entity.ApodEntity
import dev.havir.hellouniverse.infrastructure.persistence.entity.MediaTypeEntity
import dev.havir.hellouniverse.presentation.response.ApodResponse
import dev.havir.hellouniverse.presentation.response.MediaTypeResponse
import java.time.LocalDate

object TestModels {
    fun apod(
        date: LocalDate = LocalDate.of(1970, 1, 1),
        title: String = "title",
        explanation: String = "explanation",
        mediaType: MediaType = MediaType.IMAGE,
        url: String? = null,
        hdUrl: String? = null,
        thumbnailUrl: String? = null,
        copyright: String? = null,
        serviceVersion: String = "serviceVersion",
    ): Apod = Apod(
        date = date,
        title = title,
        explanation = explanation,
        mediaType = mediaType,
        url = url,
        hdUrl = hdUrl,
        thumbnailUrl = thumbnailUrl,
        copyright = copyright,
        serviceVersion = serviceVersion,
    )

    fun apodEntity(
        date: LocalDate = LocalDate.of(1970, 1, 1),
        title: String = "title",
        explanation: String = "explanation",
        mediaType: MediaTypeEntity = MediaTypeEntity.IMAGE,
        url: String? = null,
        hdUrl: String? = null,
        thumbnailUrl: String? = null,
        copyright: String? = null,
        serviceVersion: String = "serviceVersion",
    ): ApodEntity = ApodEntity(
        date = date,
        title = title,
        explanation = explanation,
        mediaType = mediaType,
        url = url,
        hdUrl = hdUrl,
        thumbnailUrl = thumbnailUrl,
        copyright = copyright,
        serviceVersion = serviceVersion,
    )

    fun apodResponse(
        date: LocalDate = LocalDate.of(1970, 1, 1),
        title: String = "title",
        explanation: String = "explanation",
        mediaType: MediaTypeResponse = MediaTypeResponse.IMAGE,
        url: String? = null,
        hdUrl: String? = null,
        thumbnailUrl: String? = null,
        copyright: String? = null,
        serviceVersion: String = "serviceVersion",
    ): ApodResponse = ApodResponse(
        date = date,
        title = title,
        explanation = explanation,
        mediaType = mediaType,
        url = url,
        hdUrl = hdUrl,
        thumbnailUrl = thumbnailUrl,
        copyright = copyright,
        serviceVersion = serviceVersion,
    )
}
