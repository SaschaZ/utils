package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.seconds
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import org.junit.jupiter.api.Test

class RunCommandTest {

    @Test
    fun testIt() = runTest(30.seconds) {
        "ls".runCommand { inStr, errStr ->
            val inJob = launchEx {
                val reader = inStr.reader()
                while (isActive && reader.ready()) print(reader.readText())
            }
            val errJob = launchEx {
                val reader = errStr.reader()
                while (isActive && reader.ready()) System.err.print(reader.readText())
            }
            inJob.cancelAndJoin()
            errJob.cancelAndJoin()
        }
    }
}