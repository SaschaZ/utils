@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.android

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogElement
import dev.zieger.utils.log.LogLevel
import dev.zieger.utils.log.PrintLn

object AndroidLog : LogElement {

    var tag = ""

    override fun log(level: LogLevel?, msg: String): String {
        when (level) {
            LogLevel.VERBOSE -> android.util.Log.v(tag, msg)
            LogLevel.DEBUG -> android.util.Log.d(tag, msg)
            LogLevel.INFO -> android.util.Log.i(tag, msg)
            LogLevel.WARNING -> android.util.Log.w(tag, msg)
            LogLevel.EXCEPTION -> android.util.Log.e(tag, msg)
            else -> android.util.Log.i(tag, msg)
        }
        return msg
    }

    fun initialize(
        tag: String? = null,
        addTime: Boolean = false,
        addCallOrigin: Boolean = false,
        useSystemOut: Boolean = false,
        logLevel: LogLevel = if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.WARNING
    ) {
        this.tag = tag ?: this.tag
        Log.clearElements(addTime = addTime, printCallOrigin = addCallOrigin, addLevelFilter = true)
        Log.plusAssign(if (useSystemOut) PrintLn else this)
    }
}