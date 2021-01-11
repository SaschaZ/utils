package dev.zieger.utils.log2.filter

import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.log2.ILogPipeline
import dev.zieger.utils.log2.LogFilter
import dev.zieger.utils.log2.logPreFilter

interface ILogLevelFilter {

    var logLevel: LogLevel

    fun copyLogLevelFilter(pipeline: ILogPipeline): ILogLevelFilter
}

class LogLevelFilter(private val pipeline: ILogPipeline) : ILogLevelFilter {

    private var activeFilter: LogFilter.LogPreFilter? = null
    override var logLevel: LogLevel by OnChanged(LogLevel.VERBOSE, notifyForInitial = true) { level ->
        activeFilter?.also { pipeline -= it }
        activeFilter = logLevelFilter(level).also { pipeline += it }
    }

    override fun copyLogLevelFilter(pipeline: ILogPipeline): ILogLevelFilter = LogLevelFilter(pipeline)
}

private fun logLevelFilter(minLevel: LogLevel) = logPreFilter {
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