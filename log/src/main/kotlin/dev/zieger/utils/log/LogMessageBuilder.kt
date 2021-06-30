@file:Suppress("ClassName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.misc.anyOf
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.misc.startsWithAny
import dev.zieger.utils.time.TimeFormat

/**
 * Log-Message-Builder
 */
interface ILogMessageBuilder : IFilter<LogPipelineContext> {

    val build: LogMessageBuilderContext.() -> String
}

open class LogMessageBuilderContext(pipelineContext: LogPipelineContext) : ILogPipelineContext by pipelineContext {

    val tagsFormatted: String?
        get() = if (tags.isNotEmpty() || messageTags.isNotEmpty())
            "[${(listOf(tag) + tags + messageTags).filterNotNull().joinToString("|")}]" else null

    fun callOrigin(
        ignorePackages: List<String> = listOf("dev.zieger.utils.log.", "dev.zieger.utils.coroutines.", "kotlin"),
        ignoreFiles: List<String> = listOf("Controllable.kt", "Observable.kt", "ExecuteExInternal.kt")
    ): String = Exception().stackTrace.firstOrNull { trace ->
        !trace.className.startsWithAny(*ignorePackages.toTypedArray())
                && trace.fileName?.anyOf(ignoreFiles) == false
    }?.run {
        "[(${fileName}:${lineNumber})#${
            (className.split(".").last().split("$").getOrNull(1)
                ?: methodName).nullWhen { it == "DefaultImpls" } ?: ""
        }]"
    } ?: ""

    fun time(format: TimeFormat = TimeFormat.TIME_ONLY) = createdAt.formatTime(format)
}

class LogMessageBuilder(
    override val build: LogMessageBuilderContext.() -> String = DEFAULT_LOG_MESSAGE
) : ILogMessageBuilder {

    companion object {

        val DEFAULT_LOG_MESSAGE: LogMessageBuilderContext.() -> String = {
            "${level.short}-${time()}: $message${tagsFormatted?.let { " - $it" } ?: ""}"
        }
        val LOG_MESSAGE_WITH_CALL_ORIGIN: LogMessageBuilderContext.() -> String = {
            "${level.short}-${time()}-${callOrigin()}: $message${tagsFormatted?.let { " - $it" } ?: ""}"
        }
    }

    override fun call(context: LogPipelineContext) {
        context.message = LogMessageBuilderContext(context).build()
    }
}