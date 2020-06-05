@file:Suppress("PropertyName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.misc.cast

interface ILogScope : ILogContext {

    val Log: ILogContext

    fun configure(
        settings: ILogSettings = Log.cast<ILogSettings>().run { copy() },
        tags: ILogTags = Log,
        filter: ILogFilters = Log.cast<ILogFilters>().run { copy() },
        builder: ILogMessageBuilder = Log,
        output: ILogOutput = Log,
        preHook: ILogPreHook = Log
    ): ILogScope
}

open class LogScopeImpl(override val Log: ILogContext = LogContext()) : ILogScope, ILogContext by Log {

    constructor(
        logSettings: ILogSettings = LogSettings(),
        logTags: ILogTags = LogTags(),
        logFilter: ILogFilters = +LogLevelFilter,
        logMsgBuilder: ILogMessageBuilder = LogElementMessageBuilder(),
        logOutput: ILogOutput = SystemPrintOutput,
        logPreHook: ILogPreHook = EmptyLogPreHook
    ) : this(LogContext(logSettings, logTags, logFilter, logMsgBuilder, logOutput, logPreHook))

    override fun configure(
        settings: ILogSettings,
        tags: ILogTags,
        filter: ILogFilters,
        builder: ILogMessageBuilder,
        output: ILogOutput,
        preHook: ILogPreHook
    ): ILogScope = LogScopeImpl(LogContext(settings, tags, filter, builder, output, preHook)).also { LogScope = it }
}