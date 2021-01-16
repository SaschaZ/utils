@file:Suppress("unused")

package dev.zieger.utils.log2

import dev.zieger.utils.core_testing.assertion2.isBlankOrNull
import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.isMatching
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log2.*
import dev.zieger.utils.log2.LogMessageBuilder.Companion.LOG_MESSAGE_WITH_CALL_ORIGIN
import dev.zieger.utils.log2.filter.LogLevel
import dev.zieger.utils.log2.filter.LogSpamFilter
import dev.zieger.utils.observable.Observable
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LogTest {

    private val messageObs = Observable<String?>("", previousValueSize = 20)
    private var message by messageObs

    @BeforeEach
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
    fun testBasics() = runTest(15.seconds) {
        Log.tag = "moofoo"
        Log += "woomoo"
        Log += "bamdam"

        Log.v("test", "fooboo", "boofoo")
        message isMatching """V-[0-9\-:]+: test - \[moofoo\|woomoo\|bamdam\|fooboo\|boofoo]"""

        message = ""
        Log.logLevel = LogLevel.DEBUG
        Log.v("test")
        message

        Log.messageBuilder = LogMessageBuilder(LOG_MESSAGE_WITH_CALL_ORIGIN)
        Log -= "woomoo"
        Log.d("test delay", filter = logPreFilter { next -> launchEx(delayed = 1.seconds) { next(this@logPreFilter) } })
        message.isBlankOrNull()
        messageObs.nextChange(5.seconds) { it isMatching """D-[0-9\-:]+-.+: test delay - \[moofoo\|bamdam\]""" }

        Log.scope {
            message = ""
            messageObs.clearPreviousValues()
            Log.tag = "foomoo"
            Log.messageBuilder = LogMessageBuilder()
            Log += LogSpamFilter(1.seconds, this@runTest)

            repeat(20) {
                Log.i("inside scope #$it")
                delay(250.milliseconds)
            }
            messageObs.previousValues.size isEqual 5
        }
        Log.i("outside scope")
        message isMatching """I-[0-9\-:]+-.+: outside scope - \[moofoo\|bamdam]"""
    }

    @Test
    fun testCalls() = runTest {
        TestClass(LogScopeImpl(Log.copy())).apply {
            testOne()
            message isMatching """V-[0-9\-:]+: testOne"""
        }
    }

    class TestClass(logScope: ILogScope) : ILogScope by logScope {
        fun testOne() = Log.v("testOne")
    }
}

