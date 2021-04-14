package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.builder.asyncEx
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.builder.withContextEx
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.base.plus
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import io.kotest.core.spec.style.FunSpec

class BuilderTest : FunSpec({

    test("launchEx") {
        var executed = false
        launchEx { executed = true }
        delay(100.milliseconds)
        assert(executed) { "executed is false" }
    }

    test("launchEx delayed") {
        var executed = false
        val delayDuration = 5.seconds
        val checkDuration = 2.seconds
        launchEx {
            withContextEx(null) {
                launchEx(delayed = delayDuration) { executed = true }
            }
        }
        delay(checkDuration)
        assert(!executed) { "executed is true" }
        delay(delayDuration - checkDuration + 1.seconds)
        assert(executed) { "executed is false" }
    }

    test("asyncEx") {
        var executed = false
        asyncEx(Unit) { executed = true }.await()
        delay(100.milliseconds)
        assert(executed) { "executed is false" }
    }

    test("withContextEx") {
        var executed = false
        withContextEx(Unit, coroutineContext) { executed = true }
        delay(100.milliseconds)
        assert(executed) { "executed is false" }
    }
})