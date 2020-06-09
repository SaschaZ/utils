@file:Suppress("unused")

package dev.zieger.utils.log

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec

class TestLogOutput : ILogOutput {

    var callCount = 0
        private set
    var lastMessage: String = ""
        private set

    override fun ILogMessageContext.write(msg: String) {
        callCount++
        lastMessage = msg

        SystemPrintOutput.run { write(msg) }
    }

    fun reset() {
        callCount = 0
        lastMessage = ""
    }
}

private val output = TestLogOutput()
private val cache = LogCache()

internal class LogTest : AnnotationSpec(), ILogScope by LogScope.configure(output = output, preHook = cache) {

    @BeforeEach
    fun beforeEach() {
        cache.reset()
        output.reset()
    }

    @Test
    fun testSpamFilter() = runTest(15.seconds) {
        Log.w("before test")
        Log.tag = "hanspans"

        repeat(100) {
            Log.i("das ist ein test $it", "logTest", "moofoo",
                filter = SpamFilter(1.seconds, "someId") { if (it == 55 || it == 60) reset() })
            delay(100.milliseconds)
            Log.i("hö?", "kkfftt")
        }
        delay(1.seconds)

        output.callCount assert 112
        cache.messages.count() assert 112
    }

    @Test
    fun testExternalFilter() = runTest(15.seconds) {
        val testVar = 10
        testVar logI {
            messageFilter += ExternalFilter(true)
            "boofoo"
        }
        delay(1.seconds)

        output.callCount assert 0
        cache.messages.count() assert 0

        testVar logI {
            messageFilter += ExternalFilter(false)
            "fooboo"
        }
        delay(1.seconds)

        output.callCount assert 1
        cache.messages.count() assert 1
    }
}