package dev.havir.hellouniverse.infrastructure.nasaapi

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.exception.ApodNotFoundException
import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType
import dev.havir.hellouniverse.infrastructure.nasaapi.dto.NasaApodDto
import dev.havir.hellouniverse.infrastructure.nasaapi.dto.NasaMediaTypeDto
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class NasaApodGateway(
    private val nasaApiRestClient: RestClient,
) : ApodGateway {
    @Retryable(
        includes = [
            HttpServerErrorException::class, // NASA 5xx
            ResourceAccessException::class, // connection errors and timeouts
        ],
        maxRetries = 3,
        delayString = "\${nasa.api.retry.delay:2000}",
        multiplier = 2.0,
    )
    override fun getApod(): Apod? {
        val dto =
            nasaApiRestClient
                .get()
                .uri { it.path("/planetary/apod").build() }
                .retrieve()
                .body<NasaApodDto>() ?: return null

        // Thrown, not returned as null, so the scheduler logs one error with
        // the real reason. Not in @Retryable's includes, so it isn't retried.
        if (dto.isPlaceholder()) {
            throw ApodNotFoundException(
                "NASA returned a placeholder for ${dto.date}",
            )
        }
        return dto.toDomain()
    }

    // Since APOD moved to science.nasa.gov, the API sometimes returns the
    // site's generic page (NASA logo + APOD's own description) instead of the
    // APOD
    private fun NasaApodDto.isPlaceholder(): Boolean =
        url == PLACEHOLDER_URL &&
            explanation.startsWith(
                "Discover the cosmos! Each day a different image",
            )

    private companion object {
        const val PLACEHOLDER_URL =
            "https://science.nasa.gov/wp-content/themes/" +
                "nasa-child/assets/images/nasa-logo@2x.png"
    }

    private fun NasaApodDto.toDomain(): Apod =
        Apod(
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

    private fun NasaMediaTypeDto.toDomain(): MediaType =
        when (this) {
            NasaMediaTypeDto.IMAGE -> MediaType.IMAGE
            NasaMediaTypeDto.VIDEO -> MediaType.VIDEO
            NasaMediaTypeDto.OTHER -> MediaType.OTHER
        }
}
