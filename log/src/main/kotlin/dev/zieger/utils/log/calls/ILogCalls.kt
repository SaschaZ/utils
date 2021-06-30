package dev.zieger.utils.log.calls

import dev.zieger.utils.log.EmptyLogFilter
import dev.zieger.utils.log.LogFilter

/**
 * Log-Calls
 */
interface ILogCalls {
    fun v(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun d(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun i(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun w(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun e(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun e(
        throwable: Throwable,
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )
}

