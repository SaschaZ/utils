package dev.zieger.utils.time

import dev.zieger.utils.json.JsonConverter
import dev.zieger.utils.time.string.DateFormat
import dev.zieger.utils.time.string.ECT
import dev.zieger.utils.time.string.GMT
import dev.zieger.utils.time.string.parse
import io.kotest.core.spec.style.FunSpec
import java.util.*

class TimeZoneTest : FunSpec({

    test("time zone") {
        val toTest = listOf(
            "13.6.2020-13:37:00" to ECT,
            "13.6.2020-13:37:00" to GMT,
            "13.6.2020-13:37:00" to TimeZone.getDefault(),
            "2020-06-13T13:37:00.000Z" to ECT,
            "2020-06-13T13:37:00.000Z" to GMT,
            "2020-06-13T13:37:00.000Z" to TimeZone.getDefault()
        )
        JsonConverter().run {
            toTest.forEach {
                val parsed = it.first.parse(it.second)
                println(
                    "${it.first}/${it.second.rawOffset} -> $parsed -> ${parsed.formatTime(DateFormat.EXCHANGE, it.second)}"
                )
            }
        }
    }

   test("jsonø") {
        val time = "1.1.2017 13:37".parse(GMT)
        println(time.formatTime(DateFormat.EXCHANGE))
    }
})