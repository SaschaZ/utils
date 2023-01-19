@file:Suppress("ClassName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.log.filter.ILogId
import dev.zieger.utils.log.filter.LogTagId
import dev.zieger.utils.misc.anyOf
import dev.zieger.utils.misc.startsWithAny
import dev.zieger.utils.time.TimeFormat

/**
 * Log-Message-Builder
 */
interface ILogMessageBuilder : IFilter<ILogMessageContext> {

    val build: LogMessageBuilderContext.() -> String
    fun copyLogMessageBuilder(): ILogMessageBuilder
}

open class LogMessageBuilderContext(context: ILogMessageContext) : ILogMessageContext by context {


    val Any?.id: Any?
        get() = (this as? ILogId)?.id

    val Any?.tag: Any?
        get() = (this as? LogTagId)?.tag ?: this

    fun callOrigin(
        ignorePackages: List<String> = listOf(
            "dev.zieger.utils",
            "kotlin", "java", "jdk", "io.kotest"
        ),
        ignoreFiles: List<String> = listOf(
            "Controllable.kt", "Observable.kt", "ExecuteExInternal.kt",
            "NativeMethodAccessorImpl.java"
        )
    ): String = Exception().stackTrace.firstOrNull { trace ->
        !trace.className.startsWithAny(*ignorePackages.toTypedArray())
                && trace.fileName?.anyOf(ignoreFiles) == false
                && trace.lineNumber >= 0
    }?.run {
        "(${fileName}:${lineNumber})"
    } ?: ""

    fun time(format: TimeFormat = TimeFormat.TIME_ONLY) = createdAt.formatTime(format)
}

class LogMessageBuilder(
    override val build: LogMessageBuilderContext.() -> String = DEFAULT_LOG_MESSAGE
) : ILogMessageBuilder {

    companion object {

        val DEFAULT_LOG_MESSAGE: LogMessageBuilderContext.() -> String = {
            val logTagId = messageTag ?: tag
            val tagToLog = logTagId?.tag
            val id = logTagId?.id
            val tagFormatted = tagToLog?.let { "$it${id?.let { id -> ":$id" } ?: ""}]" }
            "[${level.short}${tagFormatted?.let { "/$it" } ?: ""} ${time()}: $message" +
                    (throwable?.let { "\n${it.stackTraceToString()}\n" } ?: "")
        }
        val LOG_MESSAGE_WITH_CALL_ORIGIN: LogMessageBuilderContext.() -> String = {
            val logTagId = messageTag ?: tag
            val tagToLog = logTagId?.tag
            val id = logTagId?.id
            val tagFormatted = tagToLog?.let { "$it${id?.let { id -> ":$id" } ?: ""}]" }
            "[${level.short}${tagFormatted?.let { "/$it" } ?: ""} ${time()}|${callOrigin()}: $message" +
                    (throwable?.let { "\n${it.stackTraceToString()}\n" } ?: "")
        }
    }

    override fun call(context: ILogMessageContext) {
        context.buildedMessage = LogMessageBuilderContext(context).build()
    }

    override fun copyLogMessageBuilder(): ILogMessageBuilder =
        LogMessageBuilder(build)
}