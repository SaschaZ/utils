package dev.zieger.utils.core_testing

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import java.util.*

abstract class FlakyTest(private val defaultMaxExecutions: Int = 5) {

    lateinit var scope: CoroutineScope

    @Before
    open suspend fun before() = Unit
    open suspend fun beforeEach() = Unit
    open suspend fun afterEach() = Unit

    @After
    open suspend fun after() = Unit

    protected open fun runTest(
        timeout: IDurationEx = 10.seconds,
        maxExecutions: Int = defaultMaxExecutions,
        block: suspend CoroutineScope.() -> Unit
    ) = runBlocking {
        scope = DefaultCoroutineScope()
        scope.apply {
            val throwable = LinkedList<Throwable>()
            catch(
                Unit, exclude = emptyList(),
                maxExecutions = maxExecutions,
                printStackTrace = false,
                logStackTrace = false,
                onCatch = {
                    throwable += it
                    if (throwable.size == maxExecutions) {
                        System.err.println("Test failed after ${throwable.size} executions.")
                        throwable.prettyOut

                        throw it
                    } else System.err.println(
                        "Test failed at execution #${throwable.size} " +
                                "with ${it.message}. Will retry…\n\n\n\n\n"
                    )
                }) {
                beforeEach()
                withTimeout(timeout) { block() }
                afterEach()

                println("Test passed after ${throwable.size + 1} executions.\n\n")
                throwable.prettyOut
            }
        }
        scope.cancel()
    }.asUnit()
}

val List<Throwable>.prettyOut
    get() = forEachIndexed { idx, t ->
        System.err.println("\n\n${t.pretty(idx)}")
    }

fun Throwable.pretty(idx: Int) = "#${idx + 1}: ${message}\n" +
        (cause?.message?.let { "Cause: $it\n" } ?: "") +
        stackTrace.joinToString("\n")