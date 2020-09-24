package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.runTest
import org.junit.jupiter.api.Test

class RunCommandTest {

    @Test
    fun testIt() = runTest {
        "top -b -n 1 | grep java".runCommand().apply {
            if (stdOutput.isNotBlank()) println(stdOutput)
            if (errOutput.isNotBlank()) System.err.println(errOutput)
        }.also { println(it) }
    }
}