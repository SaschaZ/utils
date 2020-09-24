package dev.zieger.utils.misc

import dev.zieger.utils.core_testing.assertion2.hasSameContent
import dev.zieger.utils.core_testing.assertion2.isTrue
import dev.zieger.utils.core_testing.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FiFoTest {

    private lateinit var fifo: FiFo<Int>

    @BeforeEach
    fun beforeEach() {
        fifo = FiFo(3)
    }

    @Test
    fun testFiFo() = runTest {
        fifo.isEmpty().isTrue()

        repeat(5) { fifo.put(it) }
        fifo hasSameContent listOf(2, 3, 4)
    }
}