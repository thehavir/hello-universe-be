package dev.havir.hellouniverse.infrastructure.nasaapi.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

/**
 * One upstream APOD record from NASA's APOD Basic JSON API.
 *
 * Endpoint: `https://science.nasa.gov/wp-json/wp/v2/apod-basic`.
 * The list endpoint returns an array of these records. Appending a legacy
 * date code, such as `/261028`, returns a single record for 2026-10-28.
 *
 * For example, a video record can have an article in [url], a JPEG poster
 * in [hdUrl], and the actual MP4 source or YouTube iframe in [basicHtml].
 *
 * HTML in JSON strings can contain JSON escapes such as `\u003C` and `\n`,
 * as well as HTML character entities such as `&amp;`.
 * Optional fields can be absent, null, or empty strings.
 * An incomplete record can have an empty [explanation], a null [mediaType],
 * and a generic featured image rather than usable APOD media.
 *
 * See NASA's feed and API guide:
 * <https://api.nasa.gov/assets/files/apod/APOD-APIs-and-RSS-info.pdf>.
 */
data class NasaApodDto(
    /** APOD's calendar date, supplied as `YYYY-MM-DD`. */
    val date: LocalDate,
    /** Numeric WordPress ID of the NASA article. */
    @JsonProperty("post_id") val postId: Long,
    /** Article title; may contain HTML character entities. */
    val title: String,
    /** Canonical NASA article URL. */
    val permalink: String,
    /**
     * Explanation HTML fragment, including links and inline formatting.
     * May be empty for an incomplete upstream record.
     */
    val explanation: String,
    /**
     * Attribution to photographers, institutions, or other contributors.
     * Can contain HTML links, multiple contributors, or plain text.
     * May overlap with [copyright], but the two fields are not always equal.
     */
    val credit: String? = null,
    /**
     * Source-provided copyright/attribution content, possibly containing
     * HTML links and notices. May contain the same attribution as [credit]
     * or a linked version of that attribution.
     */
    val copyright: String? = null,
    /** Accessible description of the featured image or video poster. */
    val alt: String? = null,
    /**
     * NASA's classification; null is possible for incomplete content.
     * An iframe may contain a video player or an interactive web app.
     */
    @JsonProperty("media_type") val mediaType: NasaMediaTypeDto? = null,
    /** Article URL, normally equal to [permalink]. */
    val url: String? = null,
    /**
     * Featured image URL, or a poster for a video entry. It may include
     * image transformation parameters; the name does not guarantee an
     * untouched original. It can also be missing or a generic placeholder.
     * This field never supplies the video file itself.
     */
    @JsonProperty("hdurl") val hdUrl: String? = null,
    /**
     * Complete APOD Basic HTML document, including media, explanation,
     * credits, and navigation. The primary media can appear in `img`,
     * `video`/`source`, or `iframe` elements. URLs in the document may be
     * relative (like /images/moon.jpg) or protocol-relative (like
     * //www.youtube.com/embed/ABC). Links in the explanation can refer to
     * supporting material rather than the featured media.
     */
    @JsonProperty("basic_html") val basicHtml: String,
    /**
     * URL serving the Basic HTML document directly as plain-text HTML,
     * without a JSON wrapper. This is a source-document URL, not an
     * article permalink or playable media URL.
     */
    @JsonProperty("basic_html_url") val basicHtmlUrl: String,
)
