package dev.havir.hellouniverse.infrastructure.thumbnail

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

/**
 * Downloads over HTTPS only, with timeouts and a size limit, so a broken or
 * huge source can't fill the disk or block the scheduler.
 */
@Component
class HttpMediaFetcher(
    private val httpClient: HttpClient,
) : MediaFetcher {
    @Autowired
    constructor() : this(defaultHttpClient())

    override fun download(
        url: String,
        target: Path,
    ) = get(url) { body -> copyLimited(url, body, target) }

    override fun readText(url: String): String =
        get(url) { body ->
            val bytes = body.readNBytes(MAX_TEXT_BYTES + 1)
            if (bytes.size > MAX_TEXT_BYTES) {
                throw ThumbnailException(
                    "$url is larger than $MAX_TEXT_BYTES bytes",
                )
            }
            String(bytes)
        }

    private fun <T> get(
        url: String,
        read: (InputStream) -> T,
    ): T {
        val request =
            HttpRequest
                .newBuilder(checkedUri(url))
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", USER_AGENT)
                .GET()
                .build()
        try {
            val response =
                httpClient.send(
                    request,
                    BodyHandlers.ofInputStream(),
                )
            return response.body().use { body ->
                checkStatus(url, response.statusCode())
                read(body)
            }
        } catch (e: IOException) {
            throw ThumbnailException("Download of $url failed", e)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw ThumbnailException("Download of $url was interrupted", e)
        }
    }

    private fun checkStatus(
        url: String,
        status: Int,
    ) {
        when (status) {
            in NOT_FOUND -> throw SourceNotFoundException(
                "$url returned $status",
            )

            !in SUCCESS -> throw ThumbnailException("$url returned $status")
        }
    }

    private fun copyLimited(
        url: String,
        body: InputStream,
        target: Path,
    ) {
        Files.newOutputStream(target).use { out ->
            val buffer = ByteArray(BUFFER_BYTES)
            var total = 0L
            var read = body.read(buffer)
            while (read >= 0) {
                total += read
                if (total > MAX_DOWNLOAD_BYTES) {
                    throw ThumbnailException(
                        "$url is larger than $MAX_DOWNLOAD_BYTES bytes",
                    )
                }
                out.write(buffer, 0, read)
                read = body.read(buffer)
            }
        }
    }

    private fun checkedUri(url: String): URI {
        val uri =
            try {
                URI(url)
            } catch (e: URISyntaxException) {
                throw ThumbnailException("Invalid URL $url", e)
            }
        if (uri.scheme != "https") {
            throw ThumbnailException("Only HTTPS is allowed: $url")
        }
        return uri
    }

    private companion object {
        const val USER_AGENT = "hello-universe-thumbnails"
        val REQUEST_TIMEOUT: Duration = Duration.ofSeconds(30)
        val CONNECT_TIMEOUT: Duration = Duration.ofSeconds(5)
        const val MAX_DOWNLOAD_BYTES = 64L * 1024 * 1024
        const val MAX_TEXT_BYTES = 64 * 1024
        const val BUFFER_BYTES = 64 * 1024
        val SUCCESS = 200..299
        val NOT_FOUND = setOf(404, 410)

        // NORMAL follows redirects, but never from HTTPS to HTTP
        fun defaultHttpClient(): HttpClient =
            HttpClient
                .newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
    }
}
