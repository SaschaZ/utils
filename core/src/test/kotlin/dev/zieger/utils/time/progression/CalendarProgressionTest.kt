package dev.zieger.utils.time.progression

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.time.base.plus
import dev.zieger.utils.time.duration.days
import dev.zieger.utils.time.string.parse
import dev.zieger.utils.time.zone.UTC
import io.kotest.core.spec.style.FunSpec

internal class CalendarProgressionTest : FunSpec({

    test("calendar progression") {
        val range = "1.5.2000".parse(UTC).let { it..it + 2.days }
        range.step { addCalendarMonths(1) }.toList().size isEqual 1
        range.step { addCalendarDays(1) }.toList().size isEqual 3
    }
})