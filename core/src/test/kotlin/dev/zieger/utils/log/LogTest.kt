package dev.zieger.utils.log

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import org.junit.jupiter.api.Test

private val cache = CachingOutput()

internal class LogTest : ILogScope by LogScopeImpl(logOutput = cache) {

    @Test
    fun testSpamFilter() = runTest(15.seconds) {
        Log.w("before test")
        Log.tag = "hanspans"

        repeat(100) {
            Log.v("das ist ein test $it", "logTest", "moofoo",
                filter = SpamFilter(1.seconds, "someId") { if (it == 55 || it == 60) reset() })
            delay(100.milliseconds)
            Log.logV { tag = "kkfftt"; "hö?" }
        }
        cache.getCached().count() assert 112
    }
}