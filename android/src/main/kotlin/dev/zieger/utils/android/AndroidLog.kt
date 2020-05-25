@file:Suppress("unused")

package dev.zieger.utils.android

import dev.zieger.utils.log.*
import dev.zieger.utils.log.LogElementMessageBuilder.Companion.DEFAULT_RELEASE_LOG_ELEMENTS

object AndroidLog : ILogOutput {

    private var tag = ""

    override fun ILogMessageContext.write(msg: String) {
        when (level) {
            LogLevel.VERBOSE -> android.util.Log.v(tag, msg)
            LogLevel.DEBUG -> android.util.Log.d(tag, msg)
            LogLevel.INFO -> android.util.Log.i(tag, msg)
            LogLevel.WARNING -> android.util.Log.w(tag, msg)
            LogLevel.EXCEPTION -> android.util.Log.e(tag, msg)
            else -> android.util.Log.v("", msg)
        }
    }

    fun initialize() {
        Log.configure(builder = LogElementMessageBuilder(DEFAULT_RELEASE_LOG_ELEMENTS), output = this)
    }
}