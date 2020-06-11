@file:Suppress("PropertyName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.misc.cast

interface ILogScope : ILogContext {

    val Log: ILogContext

    fun configure(
        settings: ILogSettings = Log.cast<ILogSettings>().run { copy() },
        tags: ILogTags = Log,
        builder: ILogMessageBuilder = Log,
        elements: ILogElements = Log.cast<ILogElements>().run { copy() },
        output: ILogOutput = Log
    ): ILogScope
}

open class LogScopeImpl(override val Log: ILogContext = LogContext()) : ILogScope, ILogContext by Log {

    constructor(
        logSettings: ILogSettings = LogSettings(),
        logTags: ILogTags = LogTags(),
        iLogElements: ILogElements = LogElements(LogLevelElement),
        logMsgBuilder: ILogMessageBuilder = LogElementMessageBuilder(),
        logOutput: ILogOutput = SystemPrintOutput
    ) : this(LogContext(logSettings, logTags, logMsgBuilder, iLogElements, logOutput))

    override fun configure(
        settings: ILogSettings,
        tags: ILogTags,
        builder: ILogMessageBuilder,
        elements: ILogElements,
        output: ILogOutput
    ): ILogScope = LogScopeImpl(LogContext(settings, tags, builder, elements, output)).also { LogScope = it }
}