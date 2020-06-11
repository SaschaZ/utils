package dev.zieger.utils.log

import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.time.DateFormat
import dev.zieger.utils.time.TimeEx
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class MessageBuilder(
    private val printCallOrigin: Boolean = false,
    private val printTime: Boolean = false
) {

    fun CoroutineScope.wrapMessage(lvl: String, msg: String) =
        wrapMessage(coroutineContext, lvl, msg)

    fun wrapMessage(cc: CoroutineContext? = null, lvl: String, msg: String) =
        "${if (printTime) "$time - " else ""}$lvl:${cc.parentComponentString}-> $msg"

    private val time
        get() = TimeEx().formatTime(DateFormat.TIME_ONLY)

    private val CoroutineContext?.parentComponentString
        get() = "$coroutineDescription${if (printCallOrigin) codePosition else ""}"

    private val CoroutineContext?.coroutineDescription
        get() = this?.name?.let { "[$it]" } ?: ""

    private val CoroutineContext.name
        get() = get(CoroutineName.Key)

    private val codePosition
        get() = Exception().stackTrace.validTrace?.run { "[(${fileName}:${lineNumber})#${fixedMethodName}]" }
            ?: ""

    private val Array<StackTraceElement>.validTrace: StackTraceElement?
        get() {
            return firstOrNull { trace ->
                !trace.className.startsWith("dev.zieger.utils.log.")
                        && !trace.className.startsWith("dev.zieger.utils.coroutines.")
                        && !trace.className.startsWith("kotlin")
                        && trace.fileName?.let { fn ->
                    listOf(
                        "Controllable.kt", "Observable.kt", "ExecuteExInternal.kt"
                    ).contains(fn)
                } == false
            }
        }

    private val StackTraceElement.fixedClassName
        get() = className.split(".").last().split("$").firstOrNull()

    private val StackTraceElement.fixedMethodName
        get() = (className.split(".").last().split("$").getOrNull(1)
            ?: methodName).nullWhen { it == "DefaultImpls" } ?: ""
}