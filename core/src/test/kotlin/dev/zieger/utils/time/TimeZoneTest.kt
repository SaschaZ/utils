package dev.zieger.utils.time

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.json.JsonConverter
import dev.zieger.utils.time.string.DateFormat
import dev.zieger.utils.time.string.ECT
import dev.zieger.utils.time.string.GMT
import dev.zieger.utils.time.string.parse
import org.junit.jupiter.api.Test
import java.util.*

class TimeZoneTest {

    @Test
    fun testTimeZone() = runTest {
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

    @Test
    fun testJson() = runTest() {
        val time = "1.1.2017 13:37".parse(GMT)
        println(time.formatTime(DateFormat.EXCHANGE))
    }
}