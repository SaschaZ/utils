package dev.zieger.utils.time

import dev.zieger.utils.time.base.ITimeEx
import io.kotlintest.specs.AnnotationSpec

class TimeExTest : AnnotationSpec() {

    @Test
    fun testPrint() {
        val firstTime = TimeEx() - 5.years
        println("$firstTime")
        val secondTime = TimeEx() - 5.years * 10 / 3
        println("$secondTime")
        println("${10.minutes * 6}")
        val difference = firstTime - secondTime
        println("$difference")

        val millis = System.currentTimeMillis()
        val years = (millis / 1.years).millis
        println(years)
        val days = (millis % 1.years) / 1.days
        println(days)
        val daySum = 31 + 28 + 31 + 30 + 31 + 30 + 16
        println(daySum)
        println(TimeEx(millis).leapYears())
    }

    fun ITimeEx.leapYears(since: Int = 1970): Long = (this / 1.years / 4).toLong()
}