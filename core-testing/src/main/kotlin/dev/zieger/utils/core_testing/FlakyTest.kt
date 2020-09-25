package dev.zieger.utils.core_testing

import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.catch
import dev.zieger.utils.misc.joinToStringIndexed
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import java.util.*

abstract class FlakyTest {

    lateinit var scope: CoroutineScope

    @Before
    open suspend fun before() = Unit
    open suspend fun beforeEach() = Unit
    open suspend fun afterEach() = Unit

    @After
    open suspend fun after() = Unit

    protected open fun runTest(
        timeout: IDurationEx = 10.seconds,
        maxExecutions: Int = 5,
        block: suspend CoroutineScope.() -> Unit
    ) = runBlocking {
        scope = this

        val throwables = LinkedList<Throwable>()
        catch(Unit, exclude = emptyList(),
            maxExecutions = maxExecutions,
            printStackTrace = false,
            logStackTrace = false,
            onCatch = {
                throwables += it
                if (throwables.size == maxExecutions) {
                    System.err.println("Test failed after ${throwables.size} executions.\n\n" +
                            throwables.joinToStringIndexed("\n\n") { idx, value -> "#$idx: $value\n${value.printStackTrace()}" })
                    throw it
                } else System.err.println("Test failed at execution #${throwables.size} with\n$it. Will retry…\n\n\n\n\n")
            }) {
            beforeEach()
            withTimeout(timeout) { block() }
            afterEach()
            println("Test passed after ${throwables.size + 1} executions.\n\n" +
                    throwables.joinToStringIndexed("\n\n") { idx, value -> "#$idx: $value\n${value.printStackTrace()}" })
        }
    }.asUnit()
}