package dev.zieger.utils.log

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec

private val cache = CachingOutput()

internal class LogTest : AnnotationSpec(), ILogScope by LogScopeImpl(logOutput = cache) {

    @BeforeEach
    fun beforeEach() {
        cache.reset()
    }

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
        delay(1.seconds)
        cache.getCached().count() assert 112
    }

    @Test
    fun testExternalFilter() = runTest(15.seconds) {
        val testVar = 10
        testVar logD {
            filters += ExternalFilter(true)
            "boofoo"
        }
        delay(1.seconds)
        cache.getCached().count() assert 0

        testVar logD {
            filters += ExternalFilter(false)
            "fooboo"
        }
        delay(1.seconds)
        cache.getCached().count() assert 1
    }
}