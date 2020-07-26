package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.runEach
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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

data class ShellScope(
    val print: Boolean = true,
    val workingDir: File = File(".")
) {

    suspend operator fun String.unaryPlus(): CommandOutput? {
        print("$this: ")
        val channel = Channel<Pair<String, Boolean>>(Channel.UNLIMITED)
        val readJob = launchEx {
            for ((message, isError) in channel) {
                if (isError) System.err.println(message)
                else println(message)
            }
        }
        lateinit var runJobs: List<Job>
        val output = runCommand(workingDir) { inStr, errStr ->
            runJobs = listOf(launchEx {
                while (isActive) channel.send(inStr.readBytes().toString() to false)
            }, launchEx {
                while (isActive) channel.send(errStr.readBytes().toString() to true)
            })
        }
        runJobs.runEach { cancelAndJoin() }
        readJob.cancel()
        return output
    }
}

suspend inline fun shell(print: Boolean = true, block: ShellScope.() -> Unit) = ShellScope(print).block()

suspend fun String.runCommand(
    workingDir: File = File("."),
    block: suspend (inStr: InputStream, errStr: InputStream) -> Unit = { _, _ -> }
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
