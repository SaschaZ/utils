package dev.zieger.utils.time

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.string.DateFormat
import org.junit.jupiter.api.Test

internal class StringConverterTest {

    @Test
    fun testStringConverter() = runTest {
        println(TimeEx().formatTime(DateFormat.EXCHANGE))
    }
}