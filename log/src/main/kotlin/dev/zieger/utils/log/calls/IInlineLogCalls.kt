package dev.zieger.utils.log.calls

import dev.zieger.utils.log.EmptyLogFilter
import dev.zieger.utils.log.LogFilter


interface IInlineLogCalls {
    fun <T> T.logV(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    ): T

    fun <T> T.logD(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    ): T

    fun <T> T.logI(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    ): T

    fun <T> T.logW(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    ): T

    fun <T> T.logE(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    ): T

    fun <T> T.logE(
        throwable: Throwable,
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    ): T
}