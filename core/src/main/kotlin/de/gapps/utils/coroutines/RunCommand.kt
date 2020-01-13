package de.gapps.utils.coroutines

import java.io.File
import java.io.IOException
import java.io.InputStream

data class CommandOutput(
    val code: Int,
    val stdOutput: String?,
    val errOutput: String?
)

suspend fun String.runCommand(
    workingDir: File = File(".")
): CommandOutput? {
    var instr: InputStream? = null
    var errStr: InputStream? = null
    val input = this
    return executeNativeBlocking {
        try {
            val parts = input.split("\\s".toRegex())
            val process = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            process.waitFor()
            instr = process.inputStream
            errStr = process.errorStream
            CommandOutput(
                process.exitValue(),
                instr?.bufferedReader()?.readText(),
                errStr?.bufferedReader()?.readText()
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            instr?.close()
            errStr?.close()
        }
    }
}
