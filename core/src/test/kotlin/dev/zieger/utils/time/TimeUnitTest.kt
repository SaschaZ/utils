@file:Suppress("unused")

package dev.zieger.utils.time

import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.convert
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec

class TimeUnitTest : AnnotationSpec() {

    @Test
    fun testMillisToSeconds() {
        val millis = TimeUnit.MS
        val minutes = TimeUnit.M
        val result = (millis to minutes).convert(60000)
        assert(result == 1L) { "result should be 1L but was $result" }
    }

    @Test
    fun testHoursToMinutes() {
        val hours = TimeUnit.H
        val seconds = TimeUnit.S
        val result = (hours to seconds).convert(1)
        assert(result == 3600L) { "result should be 3600L but was $result" }
    }

    @Test
    fun testYearToDays() {
        val year = TimeUnit.YEAR
        val days = TimeUnit.DAY
        val result = (year to days).convert(10)
        assert(result == 3650L) { "result should be 3650L but was $result" }
    }

    @Test
    fun testSecondToSecond() {
        val sec0 = TimeUnit.SECOND
        val sec1 = TimeUnit.SECOND
        val result = (sec0 to sec1).convert(1)
        assert(result == 1L) { "result should be 1L but was $result" }
    }

    @Test
    fun testFormatDuration() {
        val duration = 10.seconds
        assert(duration.formatDuration() == "10S") { "format duration for 10.seconds should return 10S" }
    }
}