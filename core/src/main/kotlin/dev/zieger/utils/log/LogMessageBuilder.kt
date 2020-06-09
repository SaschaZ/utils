@file:Suppress("ClassName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.log.LogOutputElement.*
import dev.zieger.utils.misc.anyOf
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.misc.startsWithAny
import dev.zieger.utils.time.DateFormat
import dev.zieger.utils.time.DateFormat.FILENAME_TIME
import kotlinx.coroutines.CoroutineName

/**
 * Log-Message-Builder
 */
interface ILogMessageBuilder {
    var logElements: List<ILogOutputElement>

    fun ILogMessageContext.build(msg: String): String
}

interface ILogOutputElement {
    val out: ILogMessageContext.(msg: String) -> String?
}

sealed class LogOutputElement(override val out: ILogMessageContext.(msg: String) -> String?) :
    ILogOutputElement {
    class DATE(format: DateFormat = DateFormat.TIME_ONLY) : LogOutputElement({
        createdAt.formatTime(format)
    })

    object LOG_LEVEL : LogOutputElement({ level.short })

    object TAGS : LogOutputElement({ if (tags.isNotEmpty()) "[${tags.joinToString("|")}]" else "" })

    object COROUTINE_NAME : LogOutputElement({
        coroutineScope?.coroutineContext?.get(
            CoroutineName
        )?.name
    })

    class CALL_ORIGIN(
        packages: List<String> = listOf("dev.zieger.utils.log.", "dev.zieger.utils.coroutines.", "kotlin"),
        files: List<String> = listOf("Controllable.kt", "Observable.kt", "ExecuteExInternal.kt")
    ) : LogOutputElement({
        Exception().stackTrace.firstOrNull { trace ->
            !trace.className.startsWithAny(packages)
                    && trace.fileName?.anyOf(files) == false
        }?.run {
            "[(${fileName}:${lineNumber})#${(className.split(".").last().split("$").getOrNull(1)
                ?: methodName).nullWhen { it == "DefaultImpls" } ?: ""}]"
        } ?: ""
    })

    object MESSAGE : LogOutputElement({ it })

    class CUSTOM(out: ILogMessageContext.(msg: String) -> String?) : LogOutputElement(out) {
        constructor(out: String = "") : this({ out })
    }
}

open class LogElementMessageBuilder(override var logElements: List<ILogOutputElement> = DEFAULT_DEBUG_LOG_ELEMENTS) :
    ILogMessageBuilder {

    companion object {

        val DEFAULT_RELEASE_LOG_ELEMENTS = listOf(
            LOG_LEVEL,
            CUSTOM { "#" },
            COROUTINE_NAME,
            TAGS,
            CUSTOM { " " },
            DATE(FILENAME_TIME),
            CUSTOM { ": " },
            MESSAGE
        )

        val DEFAULT_DEBUG_LOG_ELEMENTS = listOf(
            LOG_LEVEL,
            CUSTOM { "#" },
            COROUTINE_NAME,
            TAGS,
            CUSTOM { " " },
            CALL_ORIGIN(),
            CUSTOM { " " },
            DATE(FILENAME_TIME),
            CUSTOM { ": " },
            MESSAGE
        )
    }

    override fun ILogMessageContext.build(msg: String): String =
        logElements.joinToString("") { it.run { out(msg) } ?: "" }
}

operator fun <T : Any> T?.unaryPlus(): MutableList<T> = listOfNotNull(this).toMutableList()
operator fun List<ILogOutputElement>.plus(text: String): List<ILogOutputElement> = this + CUSTOM(text)