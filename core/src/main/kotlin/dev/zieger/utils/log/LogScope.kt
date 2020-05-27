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
        output: ILogOutput = Log
    ): ILogContext
}

open class LogScopeImpl(override val Log: ILogContext = LogContext()) : ILogScope, ILogContext by Log {

    constructor(
        logSettings: ILogSettings = LogSettings(),
        logTags: ILogTags = LogTags(),
        logFilter: ILogFilters = +LogLevelFilter,
        logMsgBuilder: ILogMessageBuilder = LogElementMessageBuilder(),
        logOutput: ILogOutput = SystemPrintOutput
    ) :
            this(LogContext(logSettings, logTags, logFilter, logMsgBuilder, logOutput))

    override fun configure(
        settings: ILogSettings,
        tags: ILogTags,
        filter: ILogFilters,
        builder: ILogMessageBuilder,
        output: ILogOutput
    ): ILogContext = LogContext(settings, tags, filter, builder, output).also { LogScope = LogScopeImpl(it) }
}