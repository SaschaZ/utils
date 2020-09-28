package dev.zieger.utils.core_testing

import dev.zieger.utils.core_testing.assertion.assert
import org.junit.jupiter.api.Test

class TestFlakyTest : FlakyTest() {

    @Test
    fun testFlakyTest() = runTest {
        1 assert /*if (Random.nextBoolean()) 2 else*/ 1
    }
}