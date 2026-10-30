package dev.havir.hellouniverse

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class HealthEndpointTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `health endpoint reports UP without details`() {
        mockMvc.get("/actuator/health").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("UP") }
            jsonPath("$.components") { doesNotExist() }
        }
    }

    @Test
    fun `other actuator endpoints are not exposed`() {
        mockMvc.get("/actuator/env").andExpect {
            status { isNotFound() }
        }
    }
}
