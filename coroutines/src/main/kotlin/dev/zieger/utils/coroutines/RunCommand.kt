@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.coroutines

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

data class CommandOutput(
    val code: Int,
    val stdOutput: String,
    val errOutput: String
)

data class ShellScope(
    val print: Boolean = true,
    val workingDir: File = File(".")
) {

    suspend operator fun String.unaryPlus(): CommandOutput = exec()

    suspend fun String.exec(): CommandOutput = run {
        if (print) println("$this: ")
        runCommand(workingDir).apply {
            if (print) {
                if (stdOutput.isNotBlank()) println(stdOutput)
                if (errOutput.isNotBlank()) System.err.println(errOutput)
            }
        }
    }
}

suspend fun shell(print: Boolean = true, block: suspend ShellScope.() -> Unit) = ShellScope(print).block()

suspend fun String.runCommand(
    workingDir: File = File(".")
): CommandOutput = exec(workingDir).output()

suspend fun String.exec(
    workingDir: File = File(".")
): ProcessHolder = ProcessHolder(this, workingDir).exec()

data class ProcessHolder(
    private val command: String,
    private val workingDir: File
) {

    private lateinit var process: Process

    private val stdLines = LinkedList<ProcessHolder.(String) -> Unit>()
    private val errLines = LinkedList<ProcessHolder.(String) -> Unit>()

    var stdOut: String = ""
        private set
    var errOut: String = ""
        private set

    suspend fun exec(): ProcessHolder {
        if (::process.isInitialized) return this

        executeNativeBlocking {
            process = Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", command), null, workingDir)
        }
        coroutineScope {
            launch {
                process.inputStream.reader().forEachLine { line ->
                    stdOut += "$line\n"
                    stdLines.forEach { it(line) }
                }
            }
            launch {
                process.errorStream.reader().forEachLine { line ->
                    errOut += "$line\n"
                    errLines.forEach { it(line) }
                }
            }
        }

        return this
    }

    fun onStdOut(block: ProcessHolder.(String) -> Unit) = stdLines.add(block).let { { stdLines.remove(block) } }
    fun onErrOut(block: ProcessHolder.(String) -> Unit) = errLines.add(block).let { { errLines.remove(block) } }

    suspend fun wait(): ProcessHolder = apply { executeNativeBlocking { process.waitFor() } }

    suspend fun output(
        errOut: (ProcessHolder.(String) -> Unit)? = null,
        stdOut: (ProcessHolder.(String) -> Unit)? = null
    ): CommandOutput = process.run {
        val toRemove = listOfNotNull(errOut?.let { onErrOut(it) }, stdOut?.let { onStdOut(it) })
        exec()
        wait()
        CommandOutput(exitValue(), this@ProcessHolder.stdOut, this@ProcessHolder.errOut).also { _ ->
            toRemove.forEach { it() }
        }
    }
}
