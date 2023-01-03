package dev.zieger.utils.log

import dev.zieger.utils.log.filter.LogLevel.EXCEPTION

interface ILogOutput : IFilter<ILogMessageContext> {
    fun copyLogOutput(): ILogOutput
}

object SystemPrintOutput : ILogOutput {

    override fun call(context: ILogMessageContext) = when (context.level) {
        EXCEPTION -> System.err.println(context.builtMessage)
        else -> println(context.builtMessage)
    }

    override fun copyLogOutput(): ILogOutput = SystemPrintOutput
}

class LogOutput(private val block: ILogMessageContext.() -> Unit) : ILogOutput {
    override fun call(context: ILogMessageContext) = block(context)
    override fun copyLogOutput(): ILogOutput = LogOutput(block)
}