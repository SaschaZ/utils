@file:Suppress("ClassName", "unused")

package dev.zieger.utils.log

import dev.zieger.utils.log.LogOutputElement.*
import dev.zieger.utils.misc.anyOf
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.misc.startsWithAny
import dev.zieger.utils.time.string.DateFormat
import dev.zieger.utils.time.string.DateFormat.FILENAME_TIME
import dev.zieger.utils.time.string.DateFormat.TIME_ONLY
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

sealed class LogOutputElement(override val out: ILogMessageContext.(msg: String) -> String?) : ILogOutputElement {

    class DATE(format: DateFormat = TIME_ONLY) : LogOutputElement({
        createdAt.formatTime(format)
    })

    object LOG_LEVEL : LogOutputElement({ level.short })

    object TAGS : LogOutputElement({ if (tags.isNotEmpty()) "[${tags.joinToString("|")}]" else "" })

    object COROUTINE_NAME : LogOutputElement({
        coroutineScope?.coroutineContext?.get(CoroutineName)?.name
    })

    class CALL_ORIGIN(
        ignorePackages: List<String> = listOf("dev.zieger.utils.log.", "dev.zieger.utils.coroutines.", "kotlin"),
        ignoreFiles: List<String> = listOf("Controllable.kt", "Observable.kt", "ExecuteExInternal.kt")
    ) : LogOutputElement({
        Exception().stackTrace.firstOrNull { trace ->
            !trace.className.startsWithAny(ignorePackages)
                    && trace.fileName?.anyOf(ignoreFiles) == false
        }?.run {
            "[(${fileName}:${lineNumber})#${(className.split(".").last().split("$").getOrNull(1)
                ?: methodName).nullWhen { it == "DefaultImpls" } ?: ""}]"
        } ?: ""
    })

    object MESSAGE : LogOutputElement({ it })

    open class CUSTOM(out: ILogMessageContext.(msg: String) -> String?) : LogOutputElement(out) {
        constructor(out: String = "") : this({ out })
    }
}

open class LogElementMessageBuilder(override var logElements: List<ILogOutputElement> = DEFAULT_DEBUG_LOG_ELEMENTS) :
    ILogMessageBuilder {

    companion object {

        val DEFAULT_RELEASE_LOG_ELEMENTS =
            +LOG_LEVEL + "#" + COROUTINE_NAME + TAGS + " " + DATE(FILENAME_TIME) + ": " + MESSAGE

        val DEFAULT_DEBUG_LOG_ELEMENTS =
            +LOG_LEVEL + "#" + COROUTINE_NAME + TAGS + " " + CALL_ORIGIN() + " " + DATE(FILENAME_TIME) + ": " + MESSAGE
    }

    override fun ILogMessageContext.build(msg: String): String =
        logElements.joinToString("") { it.run { out(msg) } ?: "" }
}

operator fun <T : Any> T?.unaryPlus(): MutableList<T> = listOfNotNull(this).toMutableList()
operator fun List<ILogOutputElement>.plus(text: String): List<ILogOutputElement> = this + CUSTOM(text)