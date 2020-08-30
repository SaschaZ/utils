package dev.zieger.utils.log2

object SystemPrintOutput : IHook<LogPipelineContext> {

    override fun LogPipelineContext.call() = when (level) {
        LogLevel.EXCEPTION -> System.err.println(message)
        else -> println(message)
    }
}

@Suppress("FunctionName")
fun LogOutput(block: LogPipelineContext.() -> Unit) = object : IHook<LogPipelineContext> {
    override fun LogPipelineContext.call() = block()
}