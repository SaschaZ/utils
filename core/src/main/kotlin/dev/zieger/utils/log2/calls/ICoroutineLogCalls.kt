package dev.zieger.utils.log2.calls

import dev.zieger.utils.log2.EmptyPipelineLogFilter
import dev.zieger.utils.log2.IDelayFilter
import dev.zieger.utils.log2.LogPipelineContext
import kotlinx.coroutines.CoroutineScope

interface ICoroutineLogCalls {
    fun CoroutineScope.v(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun CoroutineScope.d(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun CoroutineScope.i(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun CoroutineScope.w(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun CoroutineScope.e(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun CoroutineScope.e(
        throwable: Throwable,
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )
}