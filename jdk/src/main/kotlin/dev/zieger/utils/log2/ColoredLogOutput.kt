package dev.zieger.utils.log2

import dev.zieger.utils.log.console.termColored
import dev.zieger.utils.log2.filter.LogLevel
import dev.zieger.utils.misc.asUnit

open class ColoredLogOutput : ILogOutput {
    override fun call(context: LogPipelineContext) = context.apply {
        termColored {
            println(
                when (context.level) {
                    LogLevel.VERBOSE -> brightBlue("$message")
                    LogLevel.DEBUG -> green("$message")
                    LogLevel.INFO -> yellow("$message")
                    LogLevel.WARNING -> brightMagenta("$message")
                    LogLevel.EXCEPTION -> brightRed("$message")
                    else -> cyan("$message")
                }
            )
        }
    }.asUnit()
}