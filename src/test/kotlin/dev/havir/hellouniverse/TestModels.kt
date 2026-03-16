package dev.havir.hellouniverse

import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType
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
}
