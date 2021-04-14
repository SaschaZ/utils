@file:Suppress("unused")

package dev.zieger.utils.time

import dev.zieger.utils.time.string.DateFormat
import dev.zieger.utils.time.string.parse
import dev.zieger.utils.time.zone.UTC
import io.kotest.core.spec.style.FunSpec

internal class TimeParseHelperTest : FunSpec({

    val datesToTest = listOf("2020-06-12T10:44:00")

    test("string to millis") {
        datesToTest.forEach { date ->
            println(date.parse().formatTime(DateFormat.TIME_ONLY))
        }
    }

    test("parse mex") {
        println("2018-10-31T00:00:02.826Z".parse(UTC).formatTime(DateFormat.EXCHANGE))
    }
})