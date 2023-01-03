package dev.zieger.utils.log.calls

import dev.zieger.utils.log.ILogOut
import dev.zieger.utils.log.filter.LogLevel

/**
 * Log-Calls
 */
interface ILogCalls : ILogOut {

    fun v(
        msg: Any = "",
        tag: Any? = null
    ) = out(LogLevel.VERBOSE, msg, null, null, tag)

    fun d(
        msg: Any = "",
        tag: Any? = null
    ) = out(LogLevel.DEBUG, msg, null, null, tag)

    fun i(
        msg: Any = "",
        tag: Any? = null
    ) = out(LogLevel.INFO, msg, null, null, tag)

    fun w(
        msg: Any = "",
        tag: Any? = null
    ) = out(LogLevel.WARNING, msg, null, null, tag)

    fun e(
        msg: Any = "",
        tag: Any? = null
    ) = out(LogLevel.EXCEPTION, msg, null, null, tag)

    fun e(
        throwable: Throwable,
        msg: Any = "",
        tag: Any? = null
    ) = out(LogLevel.EXCEPTION, msg, throwable, null, tag)
}

