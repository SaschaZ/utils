package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.builder.launchEx
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
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

suspend fun String.runCommand(
    workingDir: File = File("."),
    block: (output: String, isError: Boolean) -> Unit = { _, _ -> }
): CommandOutput =
    executeNativeBlocking { Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", this), null, workingDir) }.run {
        var stdOutput = ""
        val stdJob = launchEx {
            var available = 0
            while (isActive && inputStream.available().also { available = it } > 0) {
                val result = ByteArray(available)
                inputStream.read(result)
                val toString = result.toString(Charsets.UTF_8)
                stdOutput += toString
                block(toString, false)
            }
        }
        var errOutput = ""
        val errJob = launchEx {
            var available = 0
            while (isActive && inputStream.available().also { available = it } > 0) {
                val result = ByteArray(available)
                inputStream.read(result)
                val toString = result.toString(Charsets.UTF_8)
                errOutput += toString
                block(toString, true)
            }
        }
        waitFor()
        stdJob.cancelAndJoin()
        errJob.cancelAndJoin()
        CommandOutput(exitValue(), stdOutput, errOutput)
    }
