package other.pkg.filter

import dev.zieger.utils.log.*
import dev.zieger.utils.log.LogMessageBuilder.Companion.LOG_MESSAGE_WITH_CALL_ORIGIN
import dev.zieger.utils.log.filter.addTraceSpamFilter
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentLinkedQueue

class MultiThreadingTest : AnnotationSpec() {

    private lateinit var scope: ILogScope
    private lateinit var messages: ConcurrentLinkedQueue<ILogCache.LogMessage>

    @BeforeEach
    fun beforeEach() {
        messages = ConcurrentLinkedQueue()
        scope = LogScope.copy {
            messageBuilder = LogMessageBuilder(LOG_MESSAGE_WITH_CALL_ORIGIN)
            output = LogOutput {
                messages += ILogCache.LogMessage(buildedMessage, this)
                println(buildedMessage)
            }
        }
    }

    @Test
    fun testMt() = runBlocking {
        val io = CoroutineScope(Dispatchers.IO)

        val runs = 100_000
        (0 until runs).map {
            io.launch { scope.Log.v("TEST", "$it") }
        }.joinAll()

        messages.size shouldBe runs
    }

    @Test
    fun testOriginSpamFilter() = runBlocking {
        val io = CoroutineScope(Dispatchers.IO)

        scope.Log.addTraceSpamFilter()

        val runs = 100_000
        (0 until runs).map {
            io.launch {
                delay(1)
                scope.Log.v("$it", "TEST")
            }
            io.launch {
                delay(1)
                scope.Log.d("$it", "TEST")
            }
            io.launch {
                delay(1)
                scope.Log.i("$it", "TEST")
            }
        }.joinAll()
        println("all joined")

        messages.size shouldBe 3
        delay(10_000)
        messages.size shouldBe 6
    }
}