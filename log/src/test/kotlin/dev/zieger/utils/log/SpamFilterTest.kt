package dev.zieger.utils.log

import dev.zieger.utils.log.filter.LogId
import dev.zieger.utils.log.filter.spamFilter
import dev.zieger.utils.log.filter.withId
import io.kotest.core.spec.style.AnnotationSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class SpamFilterTest : AnnotationSpec() {

    object TestTag : LogId {
        override var id: Any? = null

        override fun toString(): String = this::class.simpleName!!
    }

    @BeforeEach
    fun beforeEach() {
        val scope = CoroutineScope(Dispatchers.Default)
        LogScope.reset {
            spamFilter(scope)
        }
    }

    @Test
    fun testSpamFilter() = runBlocking(Dispatchers.Default) {
        fun triggerMessage() =
            Log.v("Foo", TestTag withId 1337)

        repeat(10) {
            triggerMessage()
            delay(100)
        }
        delay(1000)
    }
}