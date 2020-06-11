package dev.zieger.utils.time

import dev.zieger.utils.time.base.div
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.base.times
import dev.zieger.utils.time.duration.minutes
import dev.zieger.utils.time.duration.years
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
    }
}