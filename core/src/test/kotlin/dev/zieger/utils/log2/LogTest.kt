@file:Suppress("unused")

package dev.zieger.utils.log2

import dev.zieger.utils.core_testing.assertion2.isBlankOrNull
import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.isMatching
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log2.*
import dev.zieger.utils.log2.LogMessageBuilder.Companion.LOG_MESSAGE_WITH_CALL_ORIGIN
import dev.zieger.utils.log2.filter.LogLevel
import dev.zieger.utils.log2.filter.LogSpamFilter
import dev.zieger.utils.observable.Observable
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import io.kotest.core.spec.style.FunSpec

internal class LogTest : FunSpec({

    class TestClass(logScope: ILogScope) : ILogScope by logScope {
        fun testOne() = Log.v("testOne")
    }

    val msgObs = Observable<String?>("", previousValueSize = 20)
    var msg by msgObs

    beforeEach {
        LogScope.reset()
        msg = ""
        msgObs.clearPreviousValues()

        Log.logLevel = LogLevel.VERBOSE
        Log.output = LogOutput {
            msg = message.toString()
            println(message)
        }
    }

    test("basic") {
        Log.tag = "moofoo"
        Log += "woomoo"
        Log += "bamdam"

        Log.v("test", "fooboo", "boofoo")
        msg isMatching """V-[0-9\-:]+: test - \[moofoo\|woomoo\|bamdam\|fooboo\|boofoo]"""

        msg = ""
        Log.logLevel = LogLevel.DEBUG
        Log.v("test")
        msg

        Log.messageBuilder = LogMessageBuilder(LOG_MESSAGE_WITH_CALL_ORIGIN)
        Log -= "woomoo"
        Log.d("test delay", filter = logPreFilter { next -> launchEx(delayed = 1.seconds) { next(this@logPreFilter) } })
        msg.isBlankOrNull()
        msgObs.nextChange(5.seconds) { it isMatching """D-[0-9\-:]+-.+: test delay - \[moofoo\|bamdam\]""" }

        Log.scope {
            msg = ""
            msgObs.clearPreviousValues()
            Log.tag = "foomoo"
            Log.messageBuilder = LogMessageBuilder()
            Log += LogSpamFilter(1.seconds, this@test)

            repeat(20) {
                Log.i("inside scope #$it")
                delay(250.milliseconds)
            }
            msgObs.previousValues.size isEqual 5
        }
        Log.i("outside scope")
        msg isMatching """I-[0-9\-:]+-.+: outside scope - \[moofoo\|bamdam]"""
    }

    test("calls") {
        TestClass(LogScopeImpl(Log.copy())).apply {
            testOne()
            msg isMatching """V-[0-9\-:]+: testOne"""
        }
    }
})

