package de.gapps.utils.android

import de.gapps.utils.log.Log
import de.gapps.utils.log.LogElement
import de.gapps.utils.log.LogLevel

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

    fun initialize(tag: String) {
        this.tag = tag
        Log.clearElements(addTime = false, addLevelFilter = true)
        Log + this
    }
}