@file:Suppress("unused")

package dev.zieger.utils.time

import dev.zieger.utils.time.string.DateFormat
import dev.zieger.utils.time.string.parse
import org.junit.jupiter.api.Test

internal class TimeParseHelperTest {

    private val datesToTest = listOf("2020-06-12T10:44:00")

    @Test
    fun stringToMillis() {
        datesToTest.forEach { date ->
            println(date.parse().formatTime(DateFormat.TIME_ONLY))
        }
    }
}