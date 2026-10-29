package dev.havir.hellouniverse.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.ZoneId

@Configuration
class ClockConfiguration {
    // APOD publishes on US Eastern time, so "today" must follow NASA's day.
    @Bean
    fun clock(): Clock = Clock.system(ZoneId.of("America/New_York"))
}
