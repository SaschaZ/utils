@file:Suppress("PropertyName", "unused")

package dev.zieger.utils.log2

interface ILogScope : ILogContext {

    val Log: ILogContext

    fun configure(
        tags: ILogTags = Log,
        pipeline: ILogPipeline = Log
    ): ILogScope
}

open class LogScopeImpl(override val Log: ILogContext = LogContext()) : ILogScope, ILogContext by Log {

    constructor(
        tags: ILogTags,
        pipeline: ILogPipeline
    ) : this(LogContext(pipeline, tags))

    override fun configure(
        tags: ILogTags,
        pipeline: ILogPipeline
    ): ILogScope = LogScopeImpl(copy(pipeline, tags)).also { LogScope = it }
}

var LogScope: ILogScope = LogScopeImpl()
    internal set