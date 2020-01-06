package de.gapps.utils.log

import de.gapps.utils.time.TimeEx
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

internal object MessageBuilder {

    fun CoroutineScope.wrapMessage(msg: Pair<String, String>) = wrapMessage(coroutineContext, msg)

    fun wrapMessage(cc: CoroutineContext?, msg: Pair<String, String>) =
        "${TimeEx()} - ${msg.first}:${buildParentComponentString(cc)}-> ${msg.second}"

    val prefix: String
        get() = "${TimeEx()} - S:${buildParentComponentString()}-> "

    private fun buildParentComponentString(cc: CoroutineContext? = null): String {
        val (className, methodName) = Exception().stackTrace.first {
            it.fixedMethodName != "DefaultImpls" && it.fileName?.let { fn ->
                listOf("Log.kt", "MessageBuilder.kt").contains(fn)
            } == false
        }.run { fixedClassName to fixedMethodName }
        return "${cc?.let { "${cc[CoroutineName]}:" } ?: ""}$className.$methodName"
    }
}

private val StackTraceElement.fixedClassName
    get() = className.split(".").last().split("$").first()

private val StackTraceElement.fixedMethodName
    get() = (className.split(".").last().split("$").getOrNull(1) ?: methodName)//.removePrefix("\$suspendImpl")