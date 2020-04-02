package dev.zieger.utils.core_testing

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.onFail
import org.junit.jupiter.api.Test

class TestExtensionsTest {

    @Test
    fun testRunTest() = runTest {
        9 onFail "boooo" assert 9
    }
}