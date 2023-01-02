package dev.zieger.utils.log.filter

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogOutput
import dev.zieger.utils.log.LogScope
import dev.zieger.utils.time.TimeFormat
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.millis
import dev.zieger.utils.time.seconds
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.runBlocking
import java.util.*

class LogSpamFilterKtTest : AnnotationSpec() {

    @Test
    fun testMessageSpamFilter() = runBlocking {
        val messages = LinkedList<LogMessage>()
        LogScope.reset {
            output = (LogOutput {
                val msg = "${createdAt.formatTime(TimeFormat.TIME_ONLY)} [${messageTag ?: tag}/${level.short}] $message"
                messages += LogMessage(level, tag, message, createdAt)
                println(msg)
            })

            addMessageSpamFilter()
        }

        suspend fun countMessages(block: suspend () -> Unit): List<LogMessage> {
            val a = LinkedList(messages)
            block()
            return messages.filterNot { it in a }
        }

        countMessages {
            repeat(10) {
                Log.v("foo", "1337")
                delay(500.millis)
            }
            delay(2.seconds)
        }.size shouldBe 2
    }
}