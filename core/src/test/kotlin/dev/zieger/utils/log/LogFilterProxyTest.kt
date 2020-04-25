package dev.zieger.utils.log

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import org.junit.jupiter.api.Test

internal class LogFilterProxyTest {

    @Test
    fun testMessageWrapper() = runTest(100.seconds) {
        println("before test")

        repeat(100) {
            Log.v("das ist ein test $it", logFilter = LogFilter.Companion.GENERIC("someId", 1.seconds, false, false))
            delay(100.milliseconds)
        }
    }
}