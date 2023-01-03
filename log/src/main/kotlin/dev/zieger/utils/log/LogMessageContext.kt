package dev.zieger.utils.log

import dev.zieger.utils.log.filter.LogLevel
import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.TimeStamp
import kotlinx.coroutines.CoroutineScope

/**
 * Log-Message-Context
 */
interface ILogMessageContext : ILogQueue, ILogTag, ICancellable {
    var level: LogLevel
    var throwable: Throwable?
    var coroutineScope: CoroutineScope?
    var createdAt: ITimeStamp
    var message: Any
    var messageTag: Any?
    var filter: IDelayFilter<ILogQueueContext>
}

class LogMessageContext(
    private val queue: ILogQueue,
    private val tags: ILogTag,
    override var level: LogLevel,
    override var throwable: Throwable? = null,
    override var message: Any = "",
    override var coroutineScope: CoroutineScope? = null,
    override var createdAt: ITimeStamp = TimeStamp(),
    override var filter: IDelayFilter<ILogQueueContext> = EmptyLogFilter,
    override var messageTag: Any? = null
) : ILogQueue by queue,
    ILogTag by tags,
    ICancellable,
    ILogMessageContext {

    override var isCancelled: Boolean = false

    override fun cancel() {
        isCancelled = true
    }

}