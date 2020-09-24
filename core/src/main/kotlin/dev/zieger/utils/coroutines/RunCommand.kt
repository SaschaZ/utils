package dev.zieger.utils.coroutines

import java.io.File

data class CommandOutput(
    val code: Int,
    val stdOutput: String,
    val errOutput: String
)

data class ShellScope(
    val print: Boolean = true,
    val workingDir: File = File(".")
) {

    suspend operator fun String.unaryPlus(): CommandOutput {
        if (print) println("$this: ")
        return runCommand(workingDir).apply {
            if (print) {
                if (stdOutput.isNotBlank()) println(stdOutput)
                if (errOutput.isNotBlank()) System.err.println(errOutput)
            }
        }
    }
}

suspend fun shell(print: Boolean = true, block: suspend ShellScope.() -> Unit) = ShellScope(print).block()

fun String.runCommand(
    workingDir: File = File(".")
): CommandOutput = Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", this), null, workingDir).run {
    waitFor()
    CommandOutput(exitValue(), inputStream.reader().readText(), errorStream.reader().readText())
}
