@file:Suppress("unused")

package dev.zieger.utils.log2test

import dev.zieger.utils.core_testing.assertion2.AssertType.MATCHES
import dev.zieger.utils.core_testing.assertion2.AssertType.NULL
import dev.zieger.utils.core_testing.assertion2.assert
import dev.zieger.utils.core_testing.assertion2.rem
import dev.zieger.utils.log2.*
import dev.zieger.utils.log2.LogMessageBuilder.Companion.LOG_MESSAGE_WITH_CALL_ORIGIN
import dev.zieger.utils.misc.FiFo
import io.kotlintest.specs.AnnotationSpec

internal class LogTest : AnnotationSpec() {

    private val messages = FiFo<Any>(Int.MAX_VALUE)

    @Before
    fun before() {
        Log.output = LogOutput {
            messages.put(message)
            println(message)
        }
    }

    @AfterEach
    fun afterEach() {
        messages.clear()
    }

    @After
    fun after() {
        Log.output = SystemPrintOutput
    }

    @Test
    fun testBasics() {
        Log.tag = "moofoo"
        Log += "woomoo"
        Log += "bamdam"
        Log.v("test", "fooboo", "boofoo")
        messages.take() to Regex("V-[0-9\\-:]+: test - \\[moofoo\\|woomoo\\|bamdam\\|fooboo\\|boofoo]") assert MATCHES
        Log.logLevel = LogLevel.DEBUG
        Log.v("test")
        messages.take() assert NULL % "no message was send"
        Log.messageBuilder = LogMessageBuilder(LOG_MESSAGE_WITH_CALL_ORIGIN)
        Log -= "woomoo"
        Log.d("test delay", hook = delayHook { Thread.sleep(1000); it(this) })

        Log.scope {
            Log.tag = "foomoo"
            Log.i("inside scope")
        }
        Log.i("outside scope")
    }
}