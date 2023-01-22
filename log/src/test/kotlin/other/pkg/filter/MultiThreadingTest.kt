package other.pkg.filter

import dev.zieger.utils.log.ILogCache
import dev.zieger.utils.log.ILogScope
import dev.zieger.utils.log.LogOutput
import dev.zieger.utils.log.LogScope
import dev.zieger.utils.log.filter.addLogSpamFilter
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.millis
import dev.zieger.utils.time.seconds
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
                messages += ILogCache.LogMessage(builtMessage, this)
                println(builtMessage)
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
        scope.Log.addLogSpamFilter()

        spamLogs()
    }

    private suspend fun singleSpam(it: Int): List<Job> =
        coroutineScope {
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
            )//.also { delay(1) }
        }

    private suspend fun spamLogs(
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

    @Test
    fun testSpamFilterDefault() = runBlocking {
        scope.apply {
            Log.addLogSpamFilter()

            repeat(40) {
                Log.v("$it", "FOOBOO")
                delay(200.millis)
            }
            delay(5.seconds)

            messages.size shouldBe 3
        }.asUnit()
    }
}