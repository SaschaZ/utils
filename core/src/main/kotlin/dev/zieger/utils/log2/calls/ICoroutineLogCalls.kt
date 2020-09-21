package dev.zieger.utils.log2.calls

import dev.zieger.utils.log2.EmptyLogFilter
import dev.zieger.utils.log2.LogFilter
import kotlinx.coroutines.CoroutineScope

interface ICoroutineLogCalls {
    fun CoroutineScope.v(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun CoroutineScope.d(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun CoroutineScope.i(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun CoroutineScope.w(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun CoroutineScope.e(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )

    fun CoroutineScope.e(
        throwable: Throwable,
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: LogFilter = EmptyLogFilter
    )
}