@file:Suppress("PropertyName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.log.filter.ILogLevelFilter
import dev.zieger.utils.misc.cast

/**
 * Holds an [ILogContext] instance.
 * Should be used to define a custom [LogContext] for a class or interface.
 */
interface ILogScope {

    val Log: ILogContext

    fun copy(
        pipeline: ILogPipeline = Log.cast<ILogPipeline>().copyPipeline(),
        tags: ILogTags = Log.cast<ILogTags>().copyTags(),
        logLevelFilter: ILogLevelFilter = Log.cast<ILogLevelFilter>().copyLogLevelFilter(pipeline),
        block: ILogContext.() -> Unit = {}
    ): ILogScope = LogScopeImpl(Log.copy(pipeline, tags, logLevelFilter, block))

    fun reset(
        pipeline: ILogPipeline = Log.cast<ILogPipeline>().copyPipeline(),
        tags: ILogTags = Log.cast<ILogTags>().copyTags(),
        logLevelFilter: ILogLevelFilter = Log.cast<ILogLevelFilter>().copyLogLevelFilter(pipeline),
        block: ILogContext.() -> Unit = {}
    )
}

open class LogScopeImpl protected constructor(override var Log: ILogContext = LogContext()) : ILogScope {

    companion object {

        operator fun invoke(context: ILogContext): ILogScope = LogScopeImpl(context.copy())
        operator fun invoke(): ILogScope = LogScopeImpl()
        operator fun invoke(configure: ILogContext.() -> Unit): ILogScope = LogScopeImpl(LogContext().apply(configure))
        operator fun invoke(tag: String): ILogScope = LogScopeImpl { this.tag = tag }
    }

    constructor(
        tags: ILogTags,
        pipeline: ILogPipeline
    ) : this(LogContext(pipeline, tags))

    override fun reset(
        pipeline: ILogPipeline,
        tags: ILogTags,
        logLevelFilter: ILogLevelFilter,
        block: ILogContext.() -> Unit
    ) {
        Log = Log.copy(pipeline, tags, logLevelFilter, block)
    }
}