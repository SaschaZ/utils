package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.TestCoroutineScope
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.milliseconds
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class CoroutineScopeExTest {

    @Test
    fun testExecute() = runBlocking {
        var executed = false
        val scope = TestCoroutineScope()
        val job = scope.launch {
            executed = true
        }
        job.join()
        assert(executed) { "block should be executed" }
    }.asUnit()

    @Test
    fun testCancelJob() = runBlocking {
        var executed = false
        val scope = TestCoroutineScope()
        val job = scope.launch {
            delay(100.milliseconds)
            if (isActive)
                executed = true
        }
        job.cancel()
        assert(!executed) { "block should not be executed" }
    }.asUnit()

    @Test
    fun testCancelScope() = runBlocking {
        var executed = false
        val scope = TestCoroutineScope()
        scope.launch {
            delay(100.milliseconds)
            if (isActive)
                executed = true
        }
        scope.cancel()
        assert(!executed) { "block should not be executed" }
    }.asUnit()

    @Test
    fun testResetScope() = runBlocking {
        var executed = false
        val scope = TestCoroutineScope()
        scope.launch {
            delay(100.milliseconds)
            if (isActive)
                executed = true
        }
        scope.reset()
        assert(!executed) { "block should not be executed" }

        executed = false
        val job = scope.launch {
            delay(100.milliseconds)
            if (isActive)
                executed = true
        }
        job.join()
        assert(executed) { "block should be executed" }
    }.asUnit()
}