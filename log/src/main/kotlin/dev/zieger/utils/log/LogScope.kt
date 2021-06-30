@file:Suppress("PropertyName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.log.filter.ILogLevelFilter

interface ILogScope : ILogContext {

    val Log: ILogContext

    fun configure(
        tags: ILogTags = Log,
        pipeline: ILogPipeline = Log
    ): ILogScope

    override fun copy(pipeline: ILogPipeline, tags: ILogTags, logLevelFilter: ILogLevelFilter): ILogScope

    fun copy(block: ILogScope.() -> Unit) = copy().apply(block)

    fun reset()
}

open class LogScopeImpl protected constructor(override val Log: ILogContext = LogContext()) : ILogScope,
    ILogContext by Log {

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

    override fun configure(
        tags: ILogTags,
        pipeline: ILogPipeline
    ): ILogScope = LogScopeImpl(copy(pipeline, tags)).also { LogScope = it }

    override fun copy(pipeline: ILogPipeline, tags: ILogTags, logLevelFilter: ILogLevelFilter): ILogScope =
        LogScopeImpl(LogContext(pipeline, tags, logLevelFilter))

    override fun reset() {
        LogScope = LogScopeImpl()
    }
}

var LogScope: ILogScope = LogScopeImpl()
    internal set