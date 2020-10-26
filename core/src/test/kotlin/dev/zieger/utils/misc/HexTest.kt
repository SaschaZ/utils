package dev.zieger.utils.misc

import dev.zieger.utils.core_testing.runTest
import org.junit.jupiter.api.Test

class HexTest {

    @Test
    fun testHex() = runTest {
        println(0x00000000L.hex4)
    }
}