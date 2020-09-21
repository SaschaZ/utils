package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.runTest
import org.junit.jupiter.api.Test

internal class ShellScopeTest {

    @Test
    fun testShellScope() = runTest {
        shell {
            +"ls"
            +"ls /"
            +"ip a"
        }
    }
}