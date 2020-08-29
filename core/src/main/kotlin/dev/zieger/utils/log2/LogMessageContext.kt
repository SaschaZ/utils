package dev.zieger.utils.log2

import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.ITimeEx
import kotlinx.coroutines.CoroutineScope

/**
 * Log-Message-Context
 */
interface ILogMessageContext : ILogContext {
    var level: LogLevel
    var throwable: Throwable?
    var coroutineScope: CoroutineScope?
    var createdAt: ITimeEx
    var message: Any
    var messageTag: List<Any>
    var hook: IDelayHook<LogPipelineContext>
}

class LogMessageContext(
    val logContext: ILogContext,
    override var level: LogLevel,
    override var throwable: Throwable? = null,
    override var message: Any = "",
    override var coroutineScope: CoroutineScope? = null,
    override var createdAt: ITimeEx = TimeEx(),
    override var hook: IDelayHook<LogPipelineContext> = EmptyPipelineLogHook,
    vararg messageTag: Any
) : ILogContext by logContext, ILogMessageContext {

    override var messageTag: List<Any> = messageTag.toList()
}