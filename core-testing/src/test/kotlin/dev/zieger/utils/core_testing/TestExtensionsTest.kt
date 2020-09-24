package dev.zieger.utils.core_testing

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.isThrowing
import dev.zieger.utils.core_testing.assertion2.msg
import dev.zieger.utils.core_testing.assertion2.rem
import org.junit.jupiter.api.Test

class TestExtensionsTest {

    @Test
    fun testRunTest() = runTest {
        9 isEqual 9 % "boooo"
        { throw IllegalArgumentException("test") } isThrowing Exception::class.msg { "" }
    }
}