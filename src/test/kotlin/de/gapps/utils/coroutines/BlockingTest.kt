package de.gapps.utils.machineex.coroutines


import de.gapps.utils.coroutines.executeNativeBlocking
import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking

class BlockingTest : AnnotationSpec() {

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