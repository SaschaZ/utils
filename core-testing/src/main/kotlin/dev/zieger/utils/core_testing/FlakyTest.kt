package dev.zieger.utils.core_testing

import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.catch
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.seconds
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

        catch(Unit, exclude = emptyList(), maxExecutions = maxExecutions,
            onCatch = {
                if (executionIdx + 1 == maxExecutions) throw it
            }) {
            beforeEach()
            withTimeout(timeout) { block() }
            afterEach()
        }
    }.asUnit()
}