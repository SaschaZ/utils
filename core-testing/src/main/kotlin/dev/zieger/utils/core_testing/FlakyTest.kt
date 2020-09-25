package dev.zieger.utils.core_testing

import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before

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

        var executionIdx = 0
        catch(Unit, exclude = emptyList(),
            maxExecutions = maxExecutions,
            printStackTrace = false,
            logStackTrace = false,
            onCatch = {
                if (executionIdx == maxExecutions) {
                    System.err.println("Test failed after $executionIdx executions.")
                    throw it
                } else System.err.println("Test failed at execution #${executionIdx} with\n$it. Will retry…\n\n\n\n\n")
            }) {
            executionIdx++
            beforeEach()
            withTimeout(timeout) { block() }
            afterEach()
        }
        println("Test passed after $executionIdx executions.")
    }.asUnit()
}