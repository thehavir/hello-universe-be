package dev.havir.hellouniverse.infrastructure.nasaapi

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType
import dev.havir.hellouniverse.infrastructure.nasaapi.dto.NasaApodDto
import dev.havir.hellouniverse.infrastructure.nasaapi.dto.NasaMediaTypeDto
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class NasaApodGateway(private val nasaApiRestClient: RestClient) : ApodGateway {
    override fun getApod(): Apod? {
        return nasaApiRestClient.get()
            .uri { it.path("/planetary/apod").build() }.retrieve()
            .body<NasaApodDto>()?.toDomain()
    }

    private fun NasaApodDto.toDomain(): Apod {
        return Apod(
            date = date,
            title = title,
            explanation = explanation,
            mediaType = mediaType.toDomain(),
            url = url,
            hdUrl = hdUrl,
            thumbnailUrl = thumbnailUrl,
            copyright = copyright,
            serviceVersion = serviceVersion,
        )
    }

    private fun NasaMediaTypeDto.toDomain(): MediaType {
        return when (this) {
            NasaMediaTypeDto.IMAGE -> MediaType.IMAGE
            NasaMediaTypeDto.VIDEO -> MediaType.VIDEO
            NasaMediaTypeDto.OTHER -> MediaType.OTHER
        }
    }
}
