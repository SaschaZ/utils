@file:Suppress("unused")

package dev.zieger.utils.log2test

import dev.zieger.utils.core_testing.assertion2.isMatching
import dev.zieger.utils.core_testing.assertion2.isNull
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log2.*
import dev.zieger.utils.log2.LogMessageBuilder.Companion.LOG_MESSAGE_WITH_CALL_ORIGIN
import dev.zieger.utils.log2.filter.LogLevel
import dev.zieger.utils.log2.filter.LogSpamFilter
import dev.zieger.utils.observable.Observable
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.milliseconds
import dev.zieger.utils.time.seconds
import io.kotlintest.specs.AnnotationSpec

internal class LogTest : AnnotationSpec() {

    private val messageObs = Observable<String?>(null)
    private var message by messageObs

    @Before
    fun before() {
        Log.logLevel = LogLevel.VERBOSE
        Log.output = LogOutput {
            this@LogTest.message = message.toString()
            println(message)
        }
    }

    @AfterEach
    fun afterEach() {
        messageObs.clearPreviousValues()
    }

    @Test
    fun testBasics() = runTest(10.seconds) {
        Log.tag = "moofoo"
        Log += "woomoo"
        Log += "bamdam"

        Log.v("test", "fooboo", "boofoo")
        message isMatching """V-[0-9\-:]+: test - \[moofoo\|woomoo\|bamdam\|fooboo\|boofoo]"""

        message = null
        Log.logLevel = LogLevel.DEBUG
        Log.v("test")
        message.isNull

        Log.messageBuilder = LogMessageBuilder(LOG_MESSAGE_WITH_CALL_ORIGIN)
        Log -= "woomoo"
        Log.d("test delay", hook = delayHook { next -> launchEx(delayed = 1.seconds) { next(this@delayHook) } })
        message.isNull
        messageObs.nextChange(5.seconds) { it isMatching """D-[0-9\-:]+-.+: test delay - \[moofoo\|bamdam\]""" }

        Log.scope {
            Log.tag = "foomoo"
            Log.messageBuilder = LogMessageBuilder()
            Log += LogSpamFilter(1.seconds)

            repeat(20) {
                message = null
                Log.i("inside scope #$it")
                if (it % 4 == 0)
                    message isMatching """I-[0-9\-:]+: inside scope #$it - \[foomoo\|bamdam]"""
                else message.isNull
                delay(250.milliseconds)
            }
        }
        Log.i("outside scope")
        message isMatching """I-[0-9\-:]+-.+: outside scope - \[moofoo\|bamdam]"""

        message = null
        messageObs.nextChange(5.seconds) { it isMatching """I-[0-9\-:]+: inside scope #19 - \[foomoo\|bamdam]""" }
    }
}

