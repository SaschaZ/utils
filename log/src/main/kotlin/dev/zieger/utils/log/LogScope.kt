@file:Suppress("PropertyName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.log.filter.ILogLevelFilter
import dev.zieger.utils.log.filter.LogLevelFilter

/**
 * Holds an [ILogContext] instance.
 */
interface ILogScope {

    val Log: ILogContext

    fun copy(
        messageBuilder: LogMessageBuilder = LogMessageBuilder(),
        logTag: ILogTag = LogTag(),
        output: ILogOutput = SystemPrintOutput,
        logQueue: ILogQueue = LogQueue(
            messageBuilder = messageBuilder,
            output = output,
            logTag = logTag
        ),
        logLevelFilter: ILogLevelFilter = LogLevelFilter(logQueue),
        block: ILogContext.() -> Unit = {}
    ): ILogScope = LogScopeImpl(
        Log.copy(messageBuilder, logTag, output, logQueue, logLevelFilter, block)
    )

    fun reset(
        messageBuilder: LogMessageBuilder = LogMessageBuilder(),
        logTag: ILogTag = LogTag(),
        output: ILogOutput = SystemPrintOutput,
        logQueue: ILogQueue = LogQueue(
            messageBuilder = messageBuilder,
            output = output,
            logTag = logTag
        ),
        logLevelFilter: ILogLevelFilter = LogLevelFilter(logQueue),
        block: ILogContext.() -> Unit = {}
    ): ILogScope {
        val log = LogContext(messageBuilder, logTag, output, logQueue, logLevelFilter)
            .apply(block)
        LogScope = LogScopeImpl(log)
        return LogScope
    }

    infix fun <T : Any?> T.logV(block: ILogMessageContext.(T) -> String) = apply {
        Log.run { logV { block(this@apply) } }
    }

    infix fun <T : Any?> T.logD(block: ILogMessageContext.(T) -> String) = apply {
        Log.run { logD { block(this@apply) } }
    }

    infix fun <T : Any?> T.logI(block: ILogMessageContext.(T) -> String) = apply {
        Log.run { logI { block(this@apply) } }
    }

    infix fun <T : Any?> T.logW(block: ILogMessageContext.(T) -> String) = apply {
        Log.run { logW { block(this@apply) } }
    }

    infix fun <T : Any?> T.logE(block: ILogMessageContext.(T) -> String) = apply {
        Log.run { logE { block(this@apply) } }
    }

    infix fun <T : Any?> T.logV(msg: String) = apply { Log.v(msg) }
    infix fun <T : Any?> T.logD(msg: String) = apply { Log.d(msg) }
    infix fun <T : Any?> T.logI(msg: String) = apply { Log.i(msg) }
    infix fun <T : Any?> T.logW(msg: String) = apply { Log.w(msg) }
    infix fun <T : Any?> T.logE(msg: String) = apply { Log.e(msg) }
}

open class LogScopeImpl protected constructor(override var Log: ILogContext = LogContext()) : ILogScope {

    companion object {

        operator fun invoke(context: ILogContext): ILogScope = LogScopeImpl(context.copy())
        operator fun invoke(): ILogScope = LogScopeImpl()
        operator fun invoke(configure: ILogContext.() -> Unit): ILogScope = LogScopeImpl(LogContext().apply(configure))
        operator fun invoke(tag: String): ILogScope = LogScopeImpl { this.tag = tag }
    }

    constructor(
        tag: ILogTag,
        queue: ILogQueue
    ) : this(LogContext(logQueue = queue, logTag = tag))
}