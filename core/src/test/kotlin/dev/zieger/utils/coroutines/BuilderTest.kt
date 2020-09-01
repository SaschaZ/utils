package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.asyncEx
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.builder.withContextEx
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.*
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking

class BuilderTest : AnnotationSpec() {

    @Test
    fun testLaunchEx() = runBlocking {
        var executed = false
        launchEx { executed = true }
        delay(100.milliseconds)
        assert(executed) { "executed is false" }
    }.asUnit()

    @Test
    fun testLaunchExDelayed() = runTest(10.seconds) {
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