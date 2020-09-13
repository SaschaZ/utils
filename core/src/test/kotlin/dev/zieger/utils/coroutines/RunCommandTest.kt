package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.seconds
import io.kotlintest.internal.isActive
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.time.delay

class RunCommandTest : AnnotationSpec() {

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