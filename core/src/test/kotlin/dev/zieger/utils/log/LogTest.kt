@file:Suppress("unused")

package dev.zieger.utils.log

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec

private val cache = LogCache()

internal class LogTest : AnnotationSpec(), ILogScope by LogScope.configure(preHook = cache) {

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
            Log.v("hö?", "kkfftt")
        }
        delay(1.seconds)
        cache.getCached().count() assert 112
    }

    @Test
    fun testExternalFilter() = runTest(15.seconds) {
        val testVar = 10
        testVar logD {
            messageFilter += ExternalFilter(true)
            "boofoo"
        }
        delay(1.seconds)
        cache.getCached().count() assert 0

        testVar logD {
            messageFilter += ExternalFilter(false)
            "fooboo"
        }
        delay(1.seconds)
        cache.getCached().count() assert 1
    }
}