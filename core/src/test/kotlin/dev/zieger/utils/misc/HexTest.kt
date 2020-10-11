package dev.zieger.utils.misc

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.runTest
import org.junit.jupiter.api.Test

class HexTest {

    @Test
    fun testHey() = runTest {
        val test = 0xAAAAAAAA
        test.hex isEqual "AAAAAAAA"
    }
}