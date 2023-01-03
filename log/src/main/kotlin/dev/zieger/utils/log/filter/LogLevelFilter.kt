package dev.zieger.utils.log.filter

import dev.zieger.utils.log.ILogQueue
import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.logPreFilter

interface ILogLevelFilter {

    var logLevel: LogLevel

    fun copyLogLevelFilter(queue: ILogQueue): ILogLevelFilter
}

class LogLevelFilter(private val queue: ILogQueue) : ILogLevelFilter {

    private var activeFilter: LogFilter.LogPreFilter? = null

    override var logLevel: LogLevel = LogLevel.VERBOSE
        set(value: LogLevel) {
            activeFilter?.also { queue.removeFilter(it) }
            activeFilter = logLevelFilter(value).also { queue.addFilter(it) }
            field = value
        }

    override fun copyLogLevelFilter(queue: ILogQueue): ILogLevelFilter = LogLevelFilter(queue)
}

private fun logLevelFilter(minLevel: LogLevel) = logPreFilter {
    when {
        level >= minLevel -> it()
        else -> cancel()
    }
}

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    EXCEPTION,
    NONE;

    val short: String
        get() = name[0].toString()
}