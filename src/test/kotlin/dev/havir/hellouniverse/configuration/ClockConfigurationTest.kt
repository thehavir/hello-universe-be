package dev.havir.hellouniverse.configuration

import java.time.ZoneId
import kotlin.test.Test
import kotlin.test.assertEquals

class ClockConfigurationTest {
    val tested = ClockConfiguration()

    @Test
    fun `on clock returns clock in America New_York zone`() {
        val result = tested.clock()

        assertEquals(ZoneId.of("America/New_York"), result.zone)
    }
}
