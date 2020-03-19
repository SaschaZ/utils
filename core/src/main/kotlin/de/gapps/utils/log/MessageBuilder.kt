package de.gapps.utils.log

import de.gapps.utils.misc.nullWhen
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

object MessageBuilder {

    fun CoroutineScope.wrapMessage(lvl: String, msg: String, addTime: Boolean = true) =
        wrapMessage(coroutineContext, lvl, msg, addTime)

    fun wrapMessage(cc: CoroutineContext? = null, lvl: String, msg: String, addTime: Boolean = true) =
        "${if (addTime) "$time - " else ""}$lvl:${cc.parentComponentString}-> $msg"

    private val time // FIXME fix time printing on Android systems
        get() = ""//TimeEx().formatTime(DateFormat.TIME_ONLY)

    private val CoroutineContext?.parentComponentString
        get() = "$coroutineDescription$codePosition"

    private val CoroutineContext?.coroutineDescription
        get() = this?.name?.let { "[$it]" } ?: ""

    private val CoroutineContext.name
        get() = get(CoroutineName.Key)

    private val codePosition
        get() = Exception().stackTrace.validTrace?.run { "[(${fileName}:${lineNumber})#${fixedMethodName}]" } ?: ""

    private val Array<StackTraceElement>.validTrace
        get() = firstOrNull { trace ->
            trace.fileName?.let { fn ->
                listOf(
                    "Controllable.kt", "Observable.kt", "ExecuteExInternal.kt"
                ).contains(fn)
            } == false
                    && !trace.className.startsWith("de.gapp.utils.log")
                    && !trace.className.startsWith("kotlin")
        }

    private val StackTraceElement.fixedClassName
        get() = className.split(".").last().split("$").firstOrNull()

    private val StackTraceElement.fixedMethodName
        get() = (className.split(".").last().split("$").getOrNull(1)
            ?: methodName).nullWhen { it == "DefaultImpls" } ?: ""
}