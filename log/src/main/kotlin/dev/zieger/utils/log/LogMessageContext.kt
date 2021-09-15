package dev.zieger.utils.log

import dev.zieger.utils.log.filter.LogLevel
import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.TimeStamp
import kotlinx.coroutines.CoroutineScope

/**
 * Log-Message-Context
 */
interface ILogMessageContext : ILogContext {
    var level: LogLevel
    var throwable: Throwable?
    var coroutineScope: CoroutineScope?
    var createdAt: ITimeStamp
    var message: Any
    var messageTag: Any?
    var filter: IDelayFilter<LogPipelineContext>
}

class LogMessageContext(
    val logContext: ILogContext,
    override var level: LogLevel,
    override var throwable: Throwable? = null,
    override var message: Any = "",
    override var coroutineScope: CoroutineScope? = null,
    override var createdAt: ITimeStamp = TimeStamp(),
    override var filter: IDelayFilter<LogPipelineContext> = EmptyLogFilter,
    override var messageTag: Any? = null
) : ILogContext by logContext, ILogMessageContext