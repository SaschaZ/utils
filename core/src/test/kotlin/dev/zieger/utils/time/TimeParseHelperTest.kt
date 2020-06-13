@file:Suppress("unused")

package dev.zieger.utils.time

import io.kotlintest.specs.AnnotationSpec

internal class TimeParseHelperTest : AnnotationSpec() {

    private val datesToTest = listOf("2020-06-12T10:44:00")

    @Test
    fun stringToMillis() {
        datesToTest.forEach { date ->
            println("${date.parse()}")
        }
    }
}