package dev.zieger.utils.coroutines


import dev.zieger.utils.misc.asUnit
import io.kotest.core.spec.style.FunSpec

class BlockingTest : FunSpec({

    fun blockingCall(): Boolean {
        Thread.sleep(100)
        return true
    }

    test("test blocking") {
        val result = executeNativeBlocking {
            Thread.sleep(100)
            blockingCall()
        }
        assert(result) { "result should be true" }
    }.asUnit()
})