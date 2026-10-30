package dev.havir.hellouniverse

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("dev")
class HellouniverseApplicationTests {
    @Test
    fun contextLoads() {
        // Fails if the application context can't start
    }
}
