package de.gapps.utils.coroutines

import de.gapps.utils.coroutines.builder.asyncEx
import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.builder.withContextEx
import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.milliseconds
import de.gapps.utils.time.duration.seconds
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking

class ExecuteTest : AnnotationSpec() {

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