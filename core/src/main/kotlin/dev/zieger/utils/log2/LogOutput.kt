package dev.zieger.utils.log2

import dev.zieger.utils.log2.filter.LogLevel.EXCEPTION

object SystemPrintOutput : IHook<LogPipelineContext> {

    override fun LogPipelineContext.call() = when (level) {
        EXCEPTION -> System.err.println(message)
        else -> println(message)
    }
}

@Suppress("FunctionName")
fun LogOutput(block: LogPipelineContext.() -> Unit) = object : IHook<LogPipelineContext> {
    override fun LogPipelineContext.call() = block()
}