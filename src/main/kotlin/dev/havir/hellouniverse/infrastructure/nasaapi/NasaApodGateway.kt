package dev.havir.hellouniverse.infrastructure.nasaapi

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.domain.model.MediaType
import dev.havir.hellouniverse.infrastructure.nasaapi.dto.NasaApodDto
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
                // Todo: Change this to fetch a list of APODs from today to the
                //  last APOD that is missing.
                .uri { it.path("/wp-json/wp/v2/apod-basic/261028").build() }
                .retrieve()
                .body<NasaApodDto>() ?: return null

        // Todo: Add validation if it's placeholder return null

        return dto.toDomain()
    }

    private fun NasaApodDto.toDomain(): Apod =
        Apod(
            date = date,
            // Todo: sanitize
            title = title,
            // Todo: sanitize
            explanation = explanation,
            // Todo: sanitize
            credit = credit,
            // Todo: sanitize
            copyright = copyright,
            // Todo: calculate the correct value
            mediaType = MediaType.OTHER,
            // Todo: calculate the correct value
            url = null,
            // Todo: calculate the correct value
            thumbnailUrl = null,
            sourceUrl = permalink,
        )
}
