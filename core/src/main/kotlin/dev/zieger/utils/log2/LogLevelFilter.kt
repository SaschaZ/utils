package dev.zieger.utils.log2

import dev.zieger.utils.delegates.OnChanged

interface ILogLevelFilter {

    var logLevel: LogLevel
}

class LogLevelFilter(private val pipeline: ILogPipeline) : ILogLevelFilter {

    override var logLevel: LogLevel by OnChanged(LogLevel.VERBOSE) {
        pipeline.preHook[0] = logLevelFilter(it)
    }
}