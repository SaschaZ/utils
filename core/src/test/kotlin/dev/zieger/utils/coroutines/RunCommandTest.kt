package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.runTest
import org.junit.jupiter.api.Test

class RunCommandTest {

    @Test
    fun testIt() = runTest {
        "top -b -n 1 | grep java".runCommand { output, isError ->
            if (isError) System.err.println(output)
            else println(output)
        }.also { println(it) }
    }
}