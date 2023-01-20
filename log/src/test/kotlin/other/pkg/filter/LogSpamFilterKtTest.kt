package other.pkg.filter

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogScope
import dev.zieger.utils.log.filter.addMessageSpamFilter
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
        val messages = LinkedList<Any>()
        LogScope.reset {
            tag = "FooWoo"
            messageBuilder.logWithOriginMethodNameName = true
            addMessageSpamFilter()
            addPostFilter { next ->
                messages += message
                next()
            }
        }

        suspend fun countMessages(block: suspend () -> Unit): List<Any> {
            val a = LinkedList(messages)
            block()
            return LinkedList(messages).filterNot { it in a }
        }

        countMessages {
            repeat(10) {
                Log.v("foo", "WOO")
                delay(500.millis)
            }
            delay(2.seconds)
        }.size shouldBe 2
    }
}