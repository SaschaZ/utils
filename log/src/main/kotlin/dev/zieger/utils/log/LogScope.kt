@file:Suppress("PropertyName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.log.filter.ILogLevelFilter
import dev.zieger.utils.log.filter.LogLevelFilter

/**
 * Holds an [ILogContext] instance.
 * Should be used to define a custom [LogContext] for a class or interface.
 */
interface ILogScope {

    val Log: ILogContext

    fun copy(
        queue: ILogQueue = Log.copy(),
        tags: ILogTag = Log.copyTags(),
        logLevelFilter: ILogLevelFilter = Log.copyLogLevelFilter(queue),
        block: ILogContext.() -> Unit = {}
    ): ILogScope = LogScopeImpl(Log.copy(queue, tags, logLevelFilter, block))

    fun reset(
        queue: ILogQueue = LogQueue(
            messageBuilder = LogMessageBuilder(),
            output = SystemPrintOutput
        ),
        tags: ILogTag = LogTag(),
        logLevelFilter: ILogLevelFilter = LogLevelFilter(queue),
        block: ILogContext.() -> Unit = {}
    )

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

    fun <T : Any?> T.logV(msg: String, filter: LogFilter = EmptyLogFilter) =
        apply { Log.v(msg, filter = filter) }

    fun <T : Any?> T.logD(msg: String, filter: LogFilter = EmptyLogFilter) =
        apply { Log.d(msg, filter = filter) }

    fun <T : Any?> T.logI(msg: String, filter: LogFilter = EmptyLogFilter) =
        apply { Log.i(msg, filter = filter) }

    fun <T : Any?> T.logW(msg: String, filter: LogFilter = EmptyLogFilter) =
        apply { Log.w(msg, filter = filter) }

    fun <T : Any?> T.logE(msg: String, filter: LogFilter = EmptyLogFilter) =
        apply { Log.e(msg, filter = filter) }
}

open class LogScopeImpl protected constructor(override var Log: ILogContext = LogContext()) : ILogScope {

    companion object {

        operator fun invoke(context: ILogContext): ILogScope = LogScopeImpl(context.copy())
        operator fun invoke(): ILogScope = LogScopeImpl()
        operator fun invoke(configure: ILogContext.() -> Unit): ILogScope = LogScopeImpl(LogContext().apply(configure))
        operator fun invoke(tag: String): ILogScope = LogScopeImpl { this.tag = tag }
    }

    constructor(
        tags: ILogTag,
        queue: ILogQueue
    ) : this(LogContext(queue, logTags = tags))

    override fun reset(
        queue: ILogQueue,
        tags: ILogTag,
        logLevelFilter: ILogLevelFilter,
        block: ILogContext.() -> Unit
    ) {
        Log = LogContext(queue, tags, logLevelFilter).apply(block)
    }
}