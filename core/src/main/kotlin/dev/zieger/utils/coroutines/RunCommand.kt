package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.builder.launchEx
import kotlinx.coroutines.isActive
import kotlinx.coroutines.joinAll
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
        return runCommand(workingDir) { output, isError ->
            if (print) {
                if (isError) System.err.println(output)
                else println(output)
            }
        }
    }
}

suspend fun shell(print: Boolean = true, block: suspend ShellScope.() -> Unit) = ShellScope(print).block()

suspend fun String.runCommand(
    workingDir: File = File("."),
    block: suspend (output: String, isError: Boolean) -> Unit = { _, _ -> }
): CommandOutput {
    val (process, outStr, outErrStr) = ProcessBuilder(*split("\\s".toRegex()).toTypedArray())
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().run { Triple(this, inputStream, errorStream) }

    var output = ""
    var errorOutput = ""
    val readJobs = listOf(launchEx {
        val reader = outStr.reader()
        while (isActive && reader.ready()) block(reader.readText().also { output += it }, false)
        outStr.close()
    }, launchEx {
        val reader = outErrStr.reader()
        while (isActive && reader.ready()) block(reader.readText().also { errorOutput += it }, true)
        outErrStr.close()
    })
    process.waitFor()
    readJobs.joinAll()

    return CommandOutput(
        process.exitValue(),
        output,
        errorOutput
    )
}
