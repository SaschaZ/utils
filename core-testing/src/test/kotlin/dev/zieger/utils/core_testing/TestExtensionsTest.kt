package dev.zieger.utils.core_testing

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import org.junit.jupiter.api.Test

class TestExtensionsTest {

    @Test
    fun testRunTest() = runTest {
        9 assert 9 % "boooo"
    }
}