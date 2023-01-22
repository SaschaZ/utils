package dev.zieger.utils.log

import dev.zieger.utils.log.filter.LogLevel
import dev.zieger.utils.time.ITimeStamp
import dev.zieger.utils.time.TimeStamp
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Log-Message-Context
 */
interface ILogMessageContext : ILogQueue, ICancellable {

    var level: LogLevel
    var throwable: Throwable?
    var coroutineScope: CoroutineScope?
    var createdAt: ITimeStamp
    var message: Any
    var messageTag: Any?
    val tag: Any?
    var delayed: Boolean
    var builtMessage: String
    var callOriginException: Exception?
}

class LogMessageContext(
    private val queue: ILogQueue,
    override var level: LogLevel,
    override var throwable: Throwable? = null,
    override var message: Any = "",
    override var coroutineScope: CoroutineScope? = null,
    override var createdAt: ITimeStamp = TimeStamp(),
    override var messageTag: Any? = null,
    override var builtMessage: String = "",
    override val tag: Any?,
    override var delayed: Boolean = false,
    override var callOriginException: Exception? = null
) : ILogQueue by queue,
    ILogMessageContext {

    override var isCancelled = AtomicBoolean(false)

    override fun cancel() = isCancelled.set(true)

}