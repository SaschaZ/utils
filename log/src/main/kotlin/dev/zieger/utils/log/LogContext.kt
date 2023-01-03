@file:Suppress("unused", "ClassName", "PropertyName")

package dev.zieger.utils.log


import dev.zieger.utils.log.calls.ILogCalls
import dev.zieger.utils.log.calls.ILogReceiverBuilder
import dev.zieger.utils.log.calls.IReceiverLogCalls
import dev.zieger.utils.log.calls.LogReceiverBuilder
import dev.zieger.utils.log.filter.ILogLevelFilter
import dev.zieger.utils.log.filter.LogLevel
import dev.zieger.utils.log.filter.LogLevelFilter
import dev.zieger.utils.time.TimeStamp
import kotlinx.coroutines.CoroutineScope

/**
 * Log-Context
 */
interface ILogContext : ILogQueue, ILogTag, ILogLevelFilter,
    ILogCalls, ILogReceiverBuilder, IReceiverLogCalls {

    fun copy(
        logQueue: ILogQueue = copyQueue(),
        tags: ILogTag = copyTags(),
        logLevelFilter: ILogLevelFilter = copyLogLevelFilter(logQueue),
        block: ILogContext.() -> Unit = {}
    ): ILogContext = LogContext(logQueue, tags, logLevelFilter).apply(block)
}

open class LogContext(
    logQueue: ILogQueue = LogQueue(
        messageBuilder = LogMessageBuilder(),
        output = SystemPrintOutput
    ),
    logTags: ILogTag = LogTag(),
    logLevelFilter: ILogLevelFilter = LogLevelFilter(logQueue)
) : ILogOut,
    ILogContext,
    ILogQueue by logQueue,
    ILogTag by logTags,
    ILogLevelFilter by logLevelFilter,
    ILogReceiverBuilder by LogReceiverBuilder(logQueue, logTags) {

    override fun out(
        lvl: LogLevel,
        msg: Any,
        filter: LogFilter,
        throwable: Throwable?,
        scope: CoroutineScope?,
        tag: Any?
    ) = LogMessageContext(this, this, lvl, throwable, msg, scope, TimeStamp(), filter, tag ?: this.tag)
        .process()
}
