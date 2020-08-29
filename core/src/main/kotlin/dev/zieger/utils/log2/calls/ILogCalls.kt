package dev.zieger.utils.log2.calls

import dev.zieger.utils.log2.EmptyPipelineLogHook
import dev.zieger.utils.log2.IDelayHook
import dev.zieger.utils.log2.LogPipelineContext

/**
 * Log-Calls
 */
interface ILogCalls {
    fun v(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun d(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun i(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun w(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun e(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun e(throwable: Throwable, msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
}

