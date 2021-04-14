@file:Suppress("PropertyName", "unused")

package dev.zieger.utils.log2

interface ILogScope : ILogContext {

    val Log: ILogContext

    fun configure(
        tags: ILogTags = Log,
        pipeline: ILogPipeline = Log
    ): ILogScope

    fun reset()
}

open class LogScopeImpl protected constructor(override val Log: ILogContext = LogContext()) : ILogScope,
    ILogContext by Log {

    companion object : () -> ILogScope, (ILogContext) -> ILogScope {

        override fun invoke(context: ILogContext): ILogScope = LogScopeImpl(context.copy())
        override fun invoke(): ILogScope = LogScopeImpl()
    }

    constructor(
        tags: ILogTags,
        pipeline: ILogPipeline
    ) : this(LogContext(pipeline, tags))

    override fun configure(
        tags: ILogTags,
        pipeline: ILogPipeline
    ): ILogScope = LogScopeImpl(copy(pipeline, tags)).also { LogScope = it }

    override fun reset() {
        LogScope = LogScopeImpl()
    }
}

var LogScope: ILogScope = LogScopeImpl()
    internal set