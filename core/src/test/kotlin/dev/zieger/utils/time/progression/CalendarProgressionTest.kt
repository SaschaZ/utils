package dev.zieger.utils.time.progression

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.time.base.plus
import dev.zieger.utils.time.duration.days
import dev.zieger.utils.time.string.parse
import dev.zieger.utils.time.zone.UTC
import org.junit.jupiter.api.Test

internal class CalendarProgressionTest {

    @Test
    fun testCalendarProgression() {
        val range = "1.5.2000".parse(UTC).let { it..it + 2.days }
        range.step { addCalendarMonths(1) }.toList().size isEqual 1
        range.step { addCalendarDays(1) }.toList().size isEqual 3
    }
}