package dev.zieger.utils.log2

import dev.zieger.utils.log2.filter.LogLevel.EXCEPTION

typealias ILogOutput = IFilter<LogPipelineContext>

object SystemPrintOutput : ILogOutput {

    override fun LogPipelineContext.call() = when (level) {
        EXCEPTION -> System.err.println(message)
        else -> println(message)
    }
}

@Suppress("FunctionName")
fun LogOutput(block: LogPipelineContext.() -> Unit) = object : ILogOutput {
    override fun LogPipelineContext.call() = block()
}