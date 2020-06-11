@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.android

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogElement
import dev.zieger.utils.log.LogLevel
import dev.zieger.utils.misc.nullWhenBlank

object AndroidLog : LogElement {

    var tag = ""
    var useSystemOut = false

    override fun log(level: LogLevel?, msg: String): String {
        if (useSystemOut) println("${tag.nullWhenBlank()?.let { "<:$it:> " } ?: ""}$msg")
        else when (level) {
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
        useSystemOut: Boolean = false
    ) {
        this.tag = tag ?: this.tag
        this.useSystemOut = useSystemOut
        Log.clearElements(addTime = addTime, printCallOrigin = addCallOrigin, addLevelFilter = true)
        Log + this
    }
}