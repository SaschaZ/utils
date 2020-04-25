package dev.zieger.utils.android

import android.content.Context
import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogElement
import dev.zieger.utils.log.LogLevel
import dev.zieger.utils.time.StringConverterDelegate

object AndroidLog : LogElement {

    private var tag = ""

    override fun log(level: LogLevel?, msg: String): String {
        when (level) {
            LogLevel.VERBOSE -> android.util.Log.v(tag, msg)
            LogLevel.DEBUG -> android.util.Log.d(tag, msg)
            LogLevel.INFO -> android.util.Log.i(tag, msg)
            LogLevel.WARNING -> android.util.Log.w(tag, msg)
            LogLevel.EXCEPTION -> android.util.Log.e(tag, msg)
            else -> android.util.Log.v("", msg)
        }
        return msg
    }

    fun initialize(context: Context, tag: String) {
        this.tag = tag
        StringConverterDelegate.formatTime = { format, zone, local ->
            "$local"
        }
        Log.clearElements(addTime = false, addLevelFilter = true)
        Log + this
    }
}