package dev.zieger.utils.coroutines


import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class BlockingTest {

    private fun blockingCall(): Boolean {
        Thread.sleep(100)
        return true
    }

    @Test
    fun testBlockingCall() = runBlocking {
        val result = executeNativeBlocking {
            Thread.sleep(100)
            blockingCall()
        }
        assert(result) { "result should be true" }
    }.asUnit()
}