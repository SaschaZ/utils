package dev.zieger.utils.time

import dev.zieger.utils.time.string.DateFormat
import io.kotest.core.spec.style.FunSpec

internal class StringConverterTest : FunSpec({

      test("string converter") {
        println(TimeEx().formatTime(DateFormat.EXCHANGE))
    }
})