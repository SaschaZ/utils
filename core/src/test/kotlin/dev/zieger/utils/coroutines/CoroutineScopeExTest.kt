package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.TestCoroutineScope
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CoroutineScopeExTest : FunSpec({

    test("execute") {
        var executed = false
        val scope = TestCoroutineScope()
        val job = scope.launch {
            executed = true
        }
        job.join()
        assert(executed) { "block should be executed" }
    }

    test("cancel job") {
        var executed = false
        val scope = TestCoroutineScope()
        val job = scope.launch {
            delay(100.milliseconds)
            if (isActive)
                executed = true
        }
        job.cancel()
        assert(!executed) { "block should not be executed" }
    }

    test("cancel scope") {
        var executed = false
        val scope = TestCoroutineScope()
        scope.launch {
            delay(100.milliseconds)
            if (isActive)
                executed = true
        }
        scope.cancel()
        assert(!executed) { "block should not be executed" }
    }

    test("reset scope") {
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
    }
})