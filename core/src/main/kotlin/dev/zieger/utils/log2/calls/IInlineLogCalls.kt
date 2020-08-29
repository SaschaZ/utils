package dev.zieger.utils.log2.calls

import dev.zieger.utils.log2.EmptyPipelineLogHook
import dev.zieger.utils.log2.IDelayHook
import dev.zieger.utils.log2.LogPipelineContext


interface IInlineLogCalls {
    fun <T> T.logV(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook): T
    fun <T> T.logD(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook): T
    fun <T> T.logI(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook): T
    fun <T> T.logW(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook): T
    fun <T> T.logE(msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook): T
    fun <T> T.logE(throwable: Throwable, msg: Any = "", vararg tag: Any = emptyArray(), hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook): T
}