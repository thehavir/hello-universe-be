package dev.havir.hellouniverse.infrastructure.nasaapi

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.infrastructure.nasaapi.dto.NasaApodDto
import dev.havir.hellouniverse.infrastructure.nasaapi.media.ApodMedia
import dev.havir.hellouniverse.infrastructure.nasaapi.media.ApodMediaExtractor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist
import org.slf4j.LoggerFactory
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Component
class NasaApodGateway(
    private val nasaApiRestClient: RestClient,
    private val apodMediaExtractor: ApodMediaExtractor,
) : ApodGateway {
    private val logger = LoggerFactory.getLogger(NasaApodGateway::class.java)

    @Retryable(
        includes = [
            HttpServerErrorException::class, // NASA 5xx
            ResourceAccessException::class, // connection errors and timeouts
        ],
        maxRetries = 3,
        delayString = $$"${nasa.api.retry.delay:2000}",
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

        val media = apodMediaExtractor.extract(dto.basicHtml)
        if (media == null) {
            logger.error("APOD {}: no media found in basic_html", dto.date)
        }

        return media?.let { dto.toDomain(it) }
    }

    private fun NasaApodDto.toDomain(media: ApodMedia): Apod =
        Apod(
            date = date,
            title = Jsoup.parseBodyFragment(title).text(),
            explanation = sanitizeHtml(explanation, permalink),
            credit = credit?.let { sanitizeHtml(it, permalink) },
            copyright = copyright?.let { sanitizeHtml(it, permalink) },
            mediaType = media.type,
            url = media.url,
            // Todo: calculate the correct value
            thumbnailUrl = null,
            sourceUrl = permalink,
        )

    private fun sanitizeHtml(
        html: String,
        baseUri: String,
    ): String =
        Jsoup.clean(
            html,
            baseUri,
            Safelist.basic(),
            Document.OutputSettings().prettyPrint(false),
        )
}
