package dev.zieger.utils.log2

import dev.zieger.utils.log.console.termColored
import dev.zieger.utils.log2.filter.LogLevel

open class ColoredLogOutput : ILogOutput {
    override fun LogPipelineContext.call() {
        termColored {
            println(
                when (this@call.level) {
                    LogLevel.VERBOSE -> brightBlue("$message")
                    LogLevel.DEBUG -> green("$message")
                    LogLevel.INFO -> yellow("$message")
                    LogLevel.WARNING -> brightMagenta("$message")
                    LogLevel.EXCEPTION -> brightRed("$message")
                    else -> cyan("$message")
                }
            )
        }
    }
}