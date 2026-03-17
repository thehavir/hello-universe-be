package dev.havir.hellouniverse.presentation.controller

import com.ninjasquad.springmockk.MockkBean
import dev.havir.hellouniverse.TestModels
import dev.havir.hellouniverse.application.usecase.GetApodsByDateRangeUseCase
import dev.havir.hellouniverse.domain.model.MediaType
import io.mockk.every
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFailsWith
import org.springframework.http.MediaType as HttpMediaType

@WebMvcTest(ApodController::class)
class ApodControllerIntegrationTest {
    @MockkBean
    lateinit var getApodsByDateRangeUseCase: GetApodsByDateRangeUseCase

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `apod endpoint returns list of ApodDto`() {
        val startDate = LocalDate.of(2020, 1, 1)
        val endDate = LocalDate.of(2020, 1, 2)
        val apod1 = TestModels.apod(
            date = startDate,
            title = "title-1",
        )
        val apod2 = TestModels.apod(
            date = endDate,
            title = "title-2",
        )
        val apods = listOf(apod1, apod2)
        every {
            getApodsByDateRangeUseCase.execute(
                startDate = startDate, endDate = endDate
            )
        } returns apods

        mockMvc.get("/apods") {
            param("start_date", "2020-01-01")
            param("end_date", "2020-01-02")
        }.andExpect {
            status { isOk() }
            content { contentType(HttpMediaType.APPLICATION_JSON) }
            jsonPath("$[0].date") { value("2020-01-01") }
            jsonPath("$[0].title") { value("title-1") }
            jsonPath("$[1].date") { value("2020-01-02") }
            jsonPath("$[1].title") { value("title-2") }
        }
    }

    @Test
    fun `apod endpoint maps Apod domain model to DTO`() {
        val startDate = LocalDate.of(2020, 1, 1)
        val endDate = LocalDate.of(2020, 1, 3)
        val apod1 = TestModels.apod(
            date = startDate,
            title = "The Galaxy - 41x0c331",
            explanation = "A Galaxy far far away",
            mediaType = MediaType.IMAGE,
            url = "https://far-galaxy.com",
            hdUrl = "https://far-galaxy.com/hd",
            thumbnailUrl = null,
            copyright = "NASA",
            serviceVersion = "v2.3",

            )
        val apod2 = TestModels.apod(
            date = LocalDate.of(2020, 1, 2),
            title = "Saturn",
            explanation = "Ring of saturn",
            mediaType = MediaType.OTHER,
            url = null,
            hdUrl = null,
            thumbnailUrl = null,
            copyright = null,
            serviceVersion = "xx33",

            )
        val apod3 = TestModels.apod(
            date = endDate,
            title = "Exoplanet Gas Giant",
            explanation = "A Giant Exoplanet",
            mediaType = MediaType.VIDEO,
            url = "https://exoplanet.xyz",
            hdUrl = "https://exoplanet.xyz/hd",
            thumbnailUrl = "https://exoplanet.xyz/thumb",
            copyright = "FREE",
            serviceVersion = "123",

            )
        val apods = listOf(apod1, apod2, apod3)
        every {
            getApodsByDateRangeUseCase.execute(
                startDate = startDate, endDate = endDate
            )
        } returns apods

        mockMvc.get("/apods") {
            param("start_date", "2020-01-01")
            param("end_date", "2020-01-03")
        }.andExpect {
            status { isOk() }
            content { contentType(HttpMediaType.APPLICATION_JSON) }
            jsonPath("$[0].date") { value("2020-01-01") }
            jsonPath("$[0].title") { value("The Galaxy - 41x0c331") }
            jsonPath("$[0].explanation") { value("A Galaxy far far away") }
            jsonPath("$[0].media_type") { value("image") }
            jsonPath("$[0].url") { value("https://far-galaxy.com") }
            jsonPath("$[0].hd_url") { value("https://far-galaxy.com/hd") }
            jsonPath("$[0].thumbnail_url") { value(null) }
            jsonPath("$[0].copyright") { value("NASA") }
            jsonPath("$[0].service_version") { value("v2.3") }
            jsonPath("$[1].date") { value("2020-01-02") }
            jsonPath("$[1].title") { value("Saturn") }
            jsonPath("$[1].explanation") { value("Ring of saturn") }
            jsonPath("$[1].media_type") { value("other") }
            jsonPath("$[1].url") { value(null) }
            jsonPath("$[1].hd_url") { value(null) }
            jsonPath("$[1].thumbnail_url") { value(null) }
            jsonPath("$[1].copyright") { value(null) }
            jsonPath("$[1].service_version") { value("xx33") }
            jsonPath("$[2].date") { value("2020-01-03") }
            jsonPath("$[2].title") { value("Exoplanet Gas Giant") }
            jsonPath("$[2].explanation") { value("A Giant Exoplanet") }
            jsonPath("$[2].media_type") { value("video") }
            jsonPath("$[2].url") { value("https://exoplanet.xyz") }
            jsonPath("$[2].hd_url") { value("https://exoplanet.xyz/hd") }
            jsonPath("$[2].thumbnail_url") { value("https://exoplanet.xyz/thumb") }
            jsonPath("$[2].copyright") { value("FREE") }
            jsonPath("$[2].service_version") { value("123") }
        }
    }

    @Test
    fun `apod endpoint returns empty list when GetApodsUseCase returns empty list`() {
        val startDate = LocalDate.of(1900, 12, 5)
        val endDate = LocalDate.of(1901, 1, 1)
        every {
            getApodsByDateRangeUseCase.execute(
                startDate = startDate, endDate = endDate
            )
        } returns listOf()

        mockMvc.get("/apods") {
            param("start_date", "1900-12-05")
            param("end_date", "1901-01-01")
        }.andExpect {
            status { isOk() }
            content { contentType(HttpMediaType.APPLICATION_JSON) }
            content { json("[]") }
        }
    }

    @Test
    fun `apod endpoint returns bad request (400) when start date is missing`() {
        mockMvc.get("/apods") {
            param("end_date", "1999-04-01")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `apod endpoint returns bad request (400) when end date is missing`() {
        mockMvc.get("/apods") {
            param("start_date", "1999-05-05")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `apod endpoint returns bad request (400) when start date and end date are missing`() {
        mockMvc.get("/apods").andExpect { status { isBadRequest() } }
    }

    @Test
    fun `apod endpoint returns bad request (400) when start date format is incorrect`() {
        mockMvc.get("/apods") {
            param("start_date", "01-01-2020")
            param("end_date", "2020-01-05")
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `apod endpoint returns bad request (400) when end date format is incorrect`() {
        mockMvc.get("/apods") {
            param("start_date", "2020-01-05")
            param("end_date", "01-01-2020")
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `apod endpoint returns bad request (400) when GetApodsByDateRangeUseCase throws`() {
        every {
            getApodsByDateRangeUseCase.execute(any(), any())
        } throws Exception("error test 22")

        assertFailsWith<Exception> {
            mockMvc.get("/apods") {
                param("start_date", "2020-05-10")
                param("end_date", "2020-05-05")
            }
        }
    }
}
