@file:Suppress("unused")

package dev.zieger.utils.android

import dev.zieger.utils.log.*
import dev.zieger.utils.log.LogElementMessageBuilder.Companion.DEFAULT_RELEASE_LOG_ELEMENTS

object AndroidLog : ILogOutput {

    private var tag = ""

    override fun ILogMessageContext.write(msg: String) {
        when (level) {
            LogLevel.VERBOSE -> android.util.Log.v(tags.firstOrNull() ?: tag, msg)
            LogLevel.DEBUG -> android.util.Log.d(tags.firstOrNull() ?: tag, msg)
            LogLevel.INFO -> android.util.Log.i(tags.firstOrNull() ?: tag, msg)
            LogLevel.WARNING -> android.util.Log.w(tags.firstOrNull() ?: tag, msg)
            LogLevel.EXCEPTION -> android.util.Log.e(tags.firstOrNull() ?: tag, msg)
        }
    }

    fun initialize() {
        LogScope.configure(builder = LogElementMessageBuilder(DEFAULT_RELEASE_LOG_ELEMENTS), output = this)
    }
}