package dev.zieger.utils.log2.filter

import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.log2.ILogPipeline
import dev.zieger.utils.log2.logPreHook

interface ILogLevelFilter {

    var logLevel: LogLevel

    fun copyLogLevelFilter(pipeline: ILogPipeline): ILogLevelFilter
}

class LogLevelFilter(private val pipeline: ILogPipeline) : ILogLevelFilter {

    override var logLevel: LogLevel by OnChanged(LogLevel.VERBOSE) {
        pipeline += logLevelFilter(it)
    }

    override fun copyLogLevelFilter(pipeline: ILogPipeline): ILogLevelFilter = LogLevelFilter(pipeline)
}

fun logLevelFilter(minLevel: LogLevel) = logPreHook {
    when {
        level >= minLevel -> it(this)
        else -> cancel()
    }
}

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    EXCEPTION;

    val short: String
        get() = name[0].toString()
}