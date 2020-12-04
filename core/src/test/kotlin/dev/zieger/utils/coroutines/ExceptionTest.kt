package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.assertion2.isFalse
import dev.zieger.utils.core_testing.assertion2.isTrue
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.asyncEx
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.builder.withContextEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log2.Log
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ExceptionTest {

    @Test
    fun testMultiLaunch() = runTest {
        val scope = DefaultCoroutineScope { _, t -> Log.e(t.localizedMessage) }
        var executed = false
        listOf(
            scope.launchEx { delay(100); executed = true },
            scope.launchEx(include = emptyList()) { throw Exception("Inside job1") }
        ).joinAll()

        executed.isFalse()

        scope.reset()
        scope.launchEx { executed = true }.join()

        executed.isTrue()
    }

    @Test
    fun testLaunch() = runBlocking {
        var catched = false
        var finally = false

        println("launchEx")
        launchEx(
            onCatch = {
                catched = true
            }, onFinally = {
                finally = true
            }) {
            println("inside launchEx")
            launchEx(delayed = 100.milliseconds) { assert(false) { "other launches should be cancelled as well" } }
            throw Throwable("throw inside launchEx")
        }
        println("after launchEx")

        delay(200L)
        assert(catched) { "throwable should be catched" }
        assert(finally) { "finally should be called" }
        println("test finished - catched=$catched; isActive=${isActive}")
    }.asUnit()

    @Test
    fun testAsync() = runBlocking {
        var catched = false
        var finally = false

        println("asyncEx")
        val result = asyncEx(
            false,
            onCatch = {
                catched = true
            }, onFinally = {
                finally = true
            }) {
            println("inside asyncEx")
            launchEx(delayed = 100.milliseconds) { assert(false) { "other launches should be cancelled as well" } }
            throw Throwable("throw inside asyncEx")
        }.await()
        println("after asyncEx")

        delay(200L)
        assert(!result) { "result should be false" }
        assert(catched) { "throwable should be catched" }
        assert(finally) { "finally should be called" }
        println("test finished - catched=$catched; isActive=${isActive}")
    }.asUnit()

    @Test
    fun testWithContext() = runBlocking {
        var catched = false
        var finally = false

        println("withContextEx")
        val result = withContextEx(false,
            coroutineContext,
            onCatch = {
                catched = true
            }, onFinally = {
                finally = true
            }) {
            println("inside withContextEx")
            if (isActive) {
                launchEx(delayed = 100.milliseconds) { assert(false) { "other launches should be cancelled as well" } }
                throw Throwable("throw inside withContextEx")
            }
            true
        }
        println("after withContextEx")

        delay(200L)
        assert(!result) { "result should be false" }
        assert(catched) { "throwable should be catched" }
        assert(finally) { "finally should be called" }
        println("test finished - catched=$catched; isActive=${isActive}")
    }.asUnit()
}