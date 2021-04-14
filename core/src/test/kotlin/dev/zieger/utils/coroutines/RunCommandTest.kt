package dev.zieger.utils.coroutines

import io.kotest.core.spec.style.FunSpec

class RunCommandTest : FunSpec({

    test("runCommand") {
        "top -b -n 1 | grep java".runCommand().apply {
            if (stdOutput.isNotBlank()) println(stdOutput)
            if (errOutput.isNotBlank()) System.err.println(errOutput)
        }.also { println(it) }
    }
})