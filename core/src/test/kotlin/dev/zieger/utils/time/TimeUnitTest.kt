@file:Suppress("unused")

package dev.zieger.utils.time

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.bigI
import dev.zieger.utils.time.base.convert
import dev.zieger.utils.time.base.plus
import dev.zieger.utils.time.duration.*
import org.junit.jupiter.api.Test

class TimeUnitTest {

    @Test
    fun testMillisToSeconds() {
        val millis = TimeUnit.MS
        val minutes = TimeUnit.M
        val result = (millis to minutes).convert(60000.bigI)
        assert(result == 1.bigI) { "result should be 1L but was $result" }
    }

    @Test
    fun testHoursToMinutes() {
        val hours = TimeUnit.H
        val seconds = TimeUnit.S
        val result = (hours to seconds).convert(1.bigI)
        assert(result == 3600.bigI) { "result should be 3600L but was $result" }
    }

    @Test
    fun testYearToDays() {
        val year = TimeUnit.YEAR
        val days = TimeUnit.DAY
        val result = (year to days).convert(10.bigI)
        assert(result == 3650.bigI) { "result should be 3650L but was $result" }
    }

    @Test
    fun testSecondToSecond() {
        val sec0 = TimeUnit.SECOND
        val sec1 = TimeUnit.SECOND
        val result = (sec0 to sec1).convert(1.bigI)
        assert(result == 1.bigI) { "result should be 1L but was $result" }
    }

    @Test
    fun testFormatDuration() {
        val duration = 10.seconds
        duration.formatDuration() assert "10S"

        val duration2 = 2.years + 3.months + 10.days + 15.hours + 34.minutes
        duration2.formatDuration() assert "2Y 3M 1W 3D 15H 34MIN"

        val duration3 = 2020.years + 5.months + 2.days
        duration3.formatDuration() assert "2020Y 5M 2D"
    }
}
