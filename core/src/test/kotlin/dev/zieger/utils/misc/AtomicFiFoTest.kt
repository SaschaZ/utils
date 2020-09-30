package dev.zieger.utils.misc

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AtomicFiFoTest {

    companion object {
        const val FIFO_CAPACITY = 10
    }

    lateinit var fifo: AtomicFiFo<Int>
    lateinit var scope: CoroutineScope

    @BeforeEach
    fun beforeEach() {
        scope = DefaultCoroutineScope()
        fifo = AtomicFiFo(FIFO_CAPACITY, scope)
    }

    @Test
    fun testFiFo() = runTest {
        repeat(FIFO_CAPACITY + 25) {
            fifo.put(it)
        }
        println(fifo.joinToString())
    }

    @AfterEach
    fun afterEach() {
        scope.cancel()
    }
}