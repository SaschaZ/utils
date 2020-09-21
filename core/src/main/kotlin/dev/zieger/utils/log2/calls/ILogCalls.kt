package dev.zieger.utils.log2.calls

import dev.zieger.utils.log2.EmptyPipelineLogFilter
import dev.zieger.utils.log2.IDelayFilter
import dev.zieger.utils.log2.LogPipelineContext

/**
 * Log-Calls
 */
interface ILogCalls {
    fun v(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun d(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun i(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun w(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun e(
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )

    fun e(
        throwable: Throwable,
        msg: Any = "",
        vararg tag: Any = emptyArray(),
        filter: IDelayFilter<LogPipelineContext> = EmptyPipelineLogFilter
    )
}

