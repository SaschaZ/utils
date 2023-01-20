package other.pkg.filter

import dev.zieger.utils.log.ILogCache
import dev.zieger.utils.log.ILogScope
import dev.zieger.utils.log.LogOutput
import dev.zieger.utils.log.LogScope
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
            messageBuilder.logWithOriginMethodNameName = true
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
    fun testOriginSpamFilter() = runBlocking(Dispatchers.IO) {
        scope.Log.addTraceSpamFilter()

        spamLogs()
    }

    private suspend fun CoroutineScope.singleSpam(it: Int): List<Job> =
        listOf(
            launch {
//                delay(1)
                scope.Log.v("$it", "TEST")
            },
            launch {
//                delay(1)
                scope.Log.d("$it", "TEST")
            },
            launch {
//                delay(1)
                scope.Log.i("$it", "TEST")
            }
        )

    private suspend fun CoroutineScope.spamLogs(
        runs: Int = 100_000,
        repeatRuns: Int = 100
    ) {
        repeat(repeatRuns) { repeat ->
            (0 until runs).flatMap {
                singleSpam(repeat * runs + it)
            }.joinAll()
//            messages.size.shouldBeGreaterThan(repeat * runs * 3)
        }
    }
}