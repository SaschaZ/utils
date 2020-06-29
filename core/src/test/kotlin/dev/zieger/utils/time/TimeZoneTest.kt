package dev.zieger.utils.time

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.json.JsonConverter
import io.kotlintest.specs.AnnotationSpec

class TimeZoneTest : AnnotationSpec() {

    @Test
    fun testTimeZone() = runTest {
        println("2015-12-15T19:00:09.928Z".parse(GMT))
        val testDate = "2018-07-08T10:00:00".parse(GMT)
        println(testDate)
        JsonConverter().run {
            println(testDate.toJson(ITimeEx::class.java))
        }
    }
}