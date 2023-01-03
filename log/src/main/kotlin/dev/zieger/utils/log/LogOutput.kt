package dev.zieger.utils.log

import dev.zieger.utils.log.filter.LogLevel.EXCEPTION

typealias ILogOutput = IFilter<ILogQueueContext>

object SystemPrintOutput : ILogOutput {

    override fun call(context: ILogQueueContext) = when (context.level) {
        EXCEPTION -> System.err.println(context.message)
        else -> println(context.message)
    }
}

@Suppress("FunctionName")
fun LogOutput(block: ILogQueueContext.() -> Unit) = object : ILogOutput {
    override fun call(context: ILogQueueContext) = block(context)
}