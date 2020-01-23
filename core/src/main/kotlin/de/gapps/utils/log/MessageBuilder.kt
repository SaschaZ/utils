package de.gapps.utils.log

import de.gapps.utils.time.TimeEx
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal object MessageBuilder {

    fun CoroutineScope.wrapMessage(msg: Pair<String, String>) = wrapMessage(coroutineContext, msg)

    fun wrapMessage(cc: CoroutineContext?, msg: Pair<String, String>) =
        "${TimeEx()} - ${msg.first}:${buildParentComponentString(cc)}-> ${msg.second}"

    private fun buildParentComponentString(cc: CoroutineContext? = null): String {
        val (fileName, methodName, lineNumber) = Exception().stackTrace.first {
            it.fixedMethodName != "DefaultImpls" && it.fileName?.let { fn ->
                listOf("Log.kt", "MessageBuilder.kt", "LogContext.kt").contains(fn)
            } == false
        }.run { Triple(fileName, fixedMethodName, lineNumber) }
        return "(${fileName}:${lineNumber})#${methodName}"
    }
}

private val StackTraceElement.fixedClassName
    get() = className.split(".").last().split("$").first()

private val StackTraceElement.fixedMethodName
    get() = (className.split(".").last().split("$").getOrNull(1) ?: methodName)//.removePrefix("\$suspendImpl")