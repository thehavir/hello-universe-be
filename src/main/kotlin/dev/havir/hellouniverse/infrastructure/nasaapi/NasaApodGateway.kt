package dev.havir.hellouniverse.infrastructure.nasaapi

import dev.havir.hellouniverse.domain.ApodGateway
import dev.havir.hellouniverse.domain.model.Apod
import dev.havir.hellouniverse.infrastructure.nasaapi.dto.NasaApodDto
import dev.havir.hellouniverse.infrastructure.nasaapi.dto.NasaErrorDto
import dev.havir.hellouniverse.infrastructure.nasaapi.media.ApodMedia
import dev.havir.hellouniverse.infrastructure.nasaapi.media.ApodMediaExtractor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.safety.Safelist
import org.springframework.resilience.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class NasaApodGateway(
    private val nasaApiRestClient: RestClient,
    private val apodMediaExtractor: ApodMediaExtractor,
) : ApodGateway {
    @Retryable(
        includes = [
            HttpServerErrorException::class, // NASA 5xx
            ResourceAccessException::class, // connection errors and timeouts
        ],
        maxRetries = 3,
        delayString = $$"${nasa.api.retry.delay:2000}",
        multiplier = 2.0,
    )
    override fun getApod(date: LocalDate): Apod? {
        val dto =
            try {
                nasaApiRestClient
                    .get()
                    .uri(APOD_PATH, date.format(DATE_CODE))
                    .retrieve()
                    .body<NasaApodDto>()
            } catch (e: HttpClientErrorException.NotFound) {
                if (e.isApodNotPublished()) null else throw e
            } ?: return null

        val media =
            apodMediaExtractor.extract(dto.basicHtml)
                ?: error("APOD ${dto.date}: no media found in basic_html")

        return dto.toDomain(media)
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

    private fun HttpClientErrorException.isApodNotPublished(): Boolean =
        getResponseBodyAs(NasaErrorDto::class.java)?.code == APOD_NOT_FOUND

    private companion object {
        const val APOD_PATH = "/wp-json/wp/v2/apod-basic/{code}"
        const val APOD_NOT_FOUND = "apod_basic_not_found"
        val DATE_CODE: DateTimeFormatter = DateTimeFormatter.ofPattern("yyMMdd")
    }
}
