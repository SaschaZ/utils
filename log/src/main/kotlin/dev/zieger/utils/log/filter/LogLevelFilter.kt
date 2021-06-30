package dev.zieger.utils.log.filter

import dev.zieger.utils.log.ILogPipeline
import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.logPreFilter
import dev.zieger.utils.observables.MutableObservable

interface ILogLevelFilter {

    var logLevel: LogLevel

    fun copyLogLevelFilter(pipeline: ILogPipeline): ILogLevelFilter
}

class LogLevelFilter(private val pipeline: ILogPipeline) : ILogLevelFilter {

    private var activeFilter: LogFilter.LogPreFilter? = null
    override var logLevel: LogLevel by MutableObservable(LogLevel.VERBOSE, notifyForInitial = true) { level ->
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