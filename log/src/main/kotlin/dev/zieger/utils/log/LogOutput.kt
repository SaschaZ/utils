package dev.zieger.utils.log

import dev.zieger.utils.log.filter.LogLevel.EXCEPTION

typealias ILogOutput = IFilter<LogPipelineContext>

object SystemPrintOutput : ILogOutput {

    override fun call(context: LogPipelineContext) = when (context.level) {
        EXCEPTION -> System.err.println(context.message)
        else -> println(context.message)
    }
}

@Suppress("FunctionName")
fun LogOutput(block: LogPipelineContext.() -> Unit) = object : ILogOutput {
    override fun call(context: LogPipelineContext) = block(context)
}