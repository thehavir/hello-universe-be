package dev.havir.hellouniverse

import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.ApodThumbnail
import dev.havir.hellouniverse.domain.model.ApodWithThumbnail
import dev.havir.hellouniverse.domain.model.MediaType
import dev.havir.hellouniverse.infrastructure.persistence.entity.ApodEntity
import dev.havir.hellouniverse.infrastructure.persistence.entity.ApodThumbnailEntity
import dev.havir.hellouniverse.infrastructure.persistence.entity.MediaTypeEntity
import dev.havir.hellouniverse.presentation.response.ApodResponse
import dev.havir.hellouniverse.presentation.response.MediaTypeResponse
import java.time.Instant
import java.time.LocalDate

object TestModels {
    fun apodWithThumbnail(
        apod: Apod = apod(),
        thumbnail: ApodThumbnail? = null,
    ): ApodWithThumbnail = ApodWithThumbnail(apod = apod, thumbnail = thumbnail)

    fun apodThumbnail(
        date: LocalDate = LocalDate.of(1970, 1, 1),
        generatedAt: Instant = Instant.EPOCH,
    ): ApodThumbnail = ApodThumbnail(date = date, generatedAt = generatedAt)

    fun apodThumbnailEntity(
        date: LocalDate = LocalDate.of(1970, 1, 1),
        generatedAt: Instant = Instant.EPOCH,
    ): ApodThumbnailEntity = ApodThumbnailEntity(date = date, generatedAt = generatedAt)

    fun apod(
        date: LocalDate = LocalDate.of(1970, 1, 1),
        title: String = "title",
        explanation: String = "explanation",
        mediaType: MediaType = MediaType.IMAGE,
        credit: String? = null,
        copyright: String? = null,
        url: String? = null,
        sourceUrl: String? = null,
    ): Apod =
        Apod(
            date = date,
            title = title,
            explanation = explanation,
            credit = credit,
            copyright = copyright,
            mediaType = mediaType,
            url = url,
            sourceUrl = sourceUrl,
        )

    fun apodEntity(
        date: LocalDate = LocalDate.of(1970, 1, 1),
        title: String = "title",
        explanation: String = "explanation",
        mediaType: MediaTypeEntity = MediaTypeEntity.IMAGE,
        credit: String? = null,
        copyright: String? = null,
        url: String? = null,
        sourceUrl: String? = null,
    ): ApodEntity =
        ApodEntity(
            date = date,
            title = title,
            explanation = explanation,
            credit = credit,
            copyright = copyright,
            mediaType = mediaType,
            url = url,
            sourceUrl = sourceUrl,
        )

    fun apodResponse(
        date: LocalDate = LocalDate.of(1970, 1, 1),
        title: String = "title",
        explanation: String = "explanation",
        mediaType: MediaTypeResponse = MediaTypeResponse.IMAGE,
        credit: String? = null,
        copyright: String? = null,
        url: String? = null,
        thumbnailUrl: String? = null,
        sourceUrl: String? = null,
    ): ApodResponse =
        ApodResponse(
            date = date,
            title = title,
            explanation = explanation,
            credit = credit,
            copyright = copyright,
            mediaType = mediaType,
            url = url,
            thumbnailUrl = thumbnailUrl,
            sourceUrl = sourceUrl,
        )
}
