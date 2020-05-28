package dev.zieger.utils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

data class CommandOutput(
    val code: Int,
    val stdOutput: String?,
    val errOutput: String?
)

suspend fun String.runCommand(
    workingDir: File = File("."),
    block: (inStr: InputStream, errStr: InputStream) -> Unit = { _, _ -> }
): CommandOutput? {
    var inStr: InputStream? = null
    var errStr: InputStream? = null
    val input = this
    val context = coroutineContext
    val scope = object : CoroutineScope {
        override val coroutineContext: CoroutineContext = context
    }
    return executeNativeBlocking {
        try {
            val parts = input.split("\\s".toRegex())
            val process = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            inStr = process.inputStream
            errStr = process.errorStream

            val job = scope.launch {
                block(inStr!!, errStr!!)
            }
            process.waitFor()
            job.cancel()

            CommandOutput(
                process.exitValue(),
                inStr?.bufferedReader()?.readText(),
                errStr?.bufferedReader()?.readText()
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            inStr?.close()
            errStr?.close()
        }
    }
}
