package dev.zieger.utils.log2.calls

import dev.zieger.utils.log2.EmptyPipelineLogHook
import dev.zieger.utils.log2.IDelayHook
import dev.zieger.utils.log2.LogPipelineContext
import kotlinx.coroutines.CoroutineScope

interface ICoroutineLogCalls {
    fun CoroutineScope.v(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun CoroutineScope.d(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun CoroutineScope.i(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun CoroutineScope.w(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun CoroutineScope.e(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
    fun CoroutineScope.e(throwable: Throwable, msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook)
}