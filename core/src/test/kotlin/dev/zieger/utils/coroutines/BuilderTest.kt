package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.builder.asyncEx
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.builder.withContextEx
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.base.plus
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class BuilderTest {

    @Test
    fun testLaunchEx() = runBlocking {
        var executed = false
        launchEx { executed = true }
        delay(100.milliseconds)
        assert(executed) { "executed is false" }
    }.asUnit()

    @Test
    fun testLaunchExDelayed() = runBlocking {
        var executed = false
        val delayDuration = 5.seconds
        val checkDuration = 2.seconds
        withContextEx(null) {
            launchEx(delayed = delayDuration) { executed = true }
        }
        withContextEx(null) {
            delay(checkDuration)
            assert(!executed) { "executed is true" }
            delay(delayDuration - checkDuration + 100.milliseconds)
            assert(executed) { "executed is false" }
        }
    }.asUnit()

    @Test
    fun testAsyncEx() = runBlocking {
        var executed = false
        asyncEx(Unit) { executed = true }.await()
        delay(100.milliseconds)
        assert(executed) { "executed is false" }
    }.asUnit()

    @Test
    fun testWithContextEx() = runBlocking {
        var executed = false
        withContextEx(Unit, coroutineContext) { executed = true }
        delay(100.milliseconds)
        assert(executed) { "executed is false" }
    }.asUnit()
}