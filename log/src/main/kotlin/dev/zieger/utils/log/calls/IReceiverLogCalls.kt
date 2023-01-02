package dev.zieger.utils.log.calls

import dev.zieger.utils.log.EmptyLogFilter
import dev.zieger.utils.log.ILogOut
import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.filter.LogLevel


interface IReceiverLogCalls : ILogOut {

    fun <T> T.logV(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ): T = apply { out(LogLevel.VERBOSE, msg, filter, null, null, tag) }

    fun <T> T.logD(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ): T = apply { out(LogLevel.DEBUG, msg, filter, null, null, tag) }

    fun <T> T.logI(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ): T = apply { out(LogLevel.INFO, msg, filter, null, null, tag) }

    fun <T> T.logW(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ): T = apply { out(LogLevel.WARNING, msg, filter, null, null, tag) }

    fun <T> T.logE(
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ): T = apply { out(LogLevel.EXCEPTION, msg, filter, null, null, tag) }

    fun <T> T.logE(
        throwable: Throwable,
        msg: Any = "",
        tag: Any? = null,
        filter: LogFilter = EmptyLogFilter
    ): T = apply { out(LogLevel.EXCEPTION, msg, filter, throwable, null, tag) }
}