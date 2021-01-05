package dev.zieger.utils.time

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.time.string.parse
import dev.zieger.utils.time.zone.UTC
import io.kotest.core.spec.style.FunSpec

class ITimeExCalendarTest : FunSpec({

    test("test add days") {
        val src = "1.3.2020".parse(UTC)
        val dst = src.addCalendarDays(1)
        dst isEqual "2.3.2020".parse(UTC)
    }
})