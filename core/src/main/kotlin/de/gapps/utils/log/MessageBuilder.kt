package de.gapps.utils.log

import de.gapps.utils.misc.nullWhen
import de.gapps.utils.time.StringConverter
import de.gapps.utils.time.TimeEx
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal object MessageBuilder {

    fun CoroutineScope.wrapMessage(lvl: String, msg: String) = wrapMessage(coroutineContext, lvl, msg)

    fun wrapMessage(cc: CoroutineContext? = null, lvl: String, msg: String) =
        "$time - $lvl:${cc.parentComponentString}-> $msg"

    private val time
        get() = TimeEx().formatTime(StringConverter.DateFormat.TIME_ONLY)

    private val CoroutineContext?.parentComponentString
        get() = "$coroutineDescription$linkedCodePosition"

    private val CoroutineContext?.coroutineDescription
        get() = this?.name?.let { "[$it]" } ?: ""

    private val CoroutineContext.name
        get() = get(CoroutineName.Key)

    private val linkedCodePosition
        get() = codePosition.run { "(${first}:${second})#${third}" }

    private val codePosition
        get() = Exception().stackTrace.run { fileName to lineNumber to methodName }

    private val Array<StackTraceElement>.lineNumber
        get() = firstOrNull()?.lineNumber

    private val Array<StackTraceElement>.fileName
        get() = firstOrNull()?.fileName?.let { listOf("Log.kt", "MessageBuilder.kt", "LogContext.kt").contains(it) }

    private val Array<StackTraceElement>.className
        get() = firstOrNull()?.className?.split(".")?.last()?.split("$")?.firstOrNull()

    private val Array<StackTraceElement>.methodName
        get() = (firstOrNull()?.className?.split(".")?.last()?.split("$")?.getOrNull(1)
            ?: firstOrNull()?.methodName).nullWhen { it == "DefaultImpls" }
}

infix fun <A, B, C> Pair<A, B>.to(c: C) = Triple(first, second, c)