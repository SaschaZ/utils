@file:Suppress("unused", "ClassName", "PropertyName")

package dev.zieger.utils.log


import dev.zieger.utils.log.calls.ILogCalls
import dev.zieger.utils.log.calls.ILogReceiverBuilder
import dev.zieger.utils.log.calls.IReceiverLogCalls
import dev.zieger.utils.log.calls.LogReceiverBuilder
import dev.zieger.utils.log.filter.ILogLevelFilter
import dev.zieger.utils.log.filter.LogLevelFilter

/**
 * Log-Context
 */
interface ILogContext : ILogQueue, ILogTag, ILogLevelFilter,
    ILogCalls, ILogReceiverBuilder, IReceiverLogCalls {

    fun copy(
        messageBuilder: ILogMessageBuilder = Log.messageBuilder.copyLogMessageBuilder(),
        logTag: ILogTag = Log.copyTags(),
        output: ILogOutput = Log.output.copyLogOutput(),
        logQueue: ILogQueue = copyQueue(),
        logLevelFilter: ILogLevelFilter = copyLogLevelFilter(logQueue),
        block: ILogContext.() -> Unit = {}
    ): ILogContext = LogContext(messageBuilder, logTag, output, logQueue, logLevelFilter).apply(block)
}

open class LogContext(
    messageBuilder: ILogMessageBuilder = LogMessageBuilder(),
    logTag: ILogTag = LogTag(),
    output: ILogOutput = SystemPrintOutput,
    logQueue: ILogQueue = LogQueue(
        messageBuilder = messageBuilder,
        output = output,
        logTag = logTag
    ),
    logLevelFilter: ILogLevelFilter = LogLevelFilter(logQueue)
) : ILogOut by LogOut(logQueue, logTag),
    ILogContext,
    ILogQueue by logQueue,
    ILogTag by logTag,
    ILogLevelFilter by logLevelFilter,
    ILogReceiverBuilder by LogReceiverBuilder(logQueue, logTag) {
}