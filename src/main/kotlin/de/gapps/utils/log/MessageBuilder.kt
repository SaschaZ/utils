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
        val (className, methodName) = Exception().stackTrace.first { it.fileName != "Log.kt" }.run {
            val cn = className.split(".").last().split("$").run { first() to getOrNull(1) }
            val mn = (cn.second ?: methodName).removePrefix("\$suspendImpl")
            Pair(cn.first, mn)
        }
        return "${cc?.let { "${cc[CoroutineName]}:" } ?: ""}$className.$methodName"
    }
}