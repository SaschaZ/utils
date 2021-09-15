package dev.zieger.utils.log.calls

import dev.zieger.utils.log.EmptyLogFilter
import dev.zieger.utils.log.ILogOut
import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.filter.LogLevel

/**
 * Log-Calls
 */
interface ILogCalls : ILogOut {

    fun v(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ) = out(LogLevel.VERBOSE, msg, filter, null, null, tag)

    fun d(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ) = out(LogLevel.DEBUG, msg, filter, null, null, tag)

    fun i(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ) = out(LogLevel.INFO, msg, filter, null, null, tag)

    fun w(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ) = out(LogLevel.WARNING, msg, filter, null, null, tag)

    fun e(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ) = out(LogLevel.EXCEPTION, msg, filter, null, null, tag)

    fun e(
        throwable: Throwable,
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ) = out(LogLevel.EXCEPTION, msg, filter, throwable, null, tag)
}

