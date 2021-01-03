package dev.zieger.utils.log2

import dev.zieger.utils.log2.filter.LogLevel
import dev.zieger.utils.misc.asUnit

open class AndroidLogOutput : ILogOutput {
    override fun call(context: LogPipelineContext) = context.run {
        when (level) {
            LogLevel.VERBOSE -> android.util.Log.v("${tag ?: ""}", "$message")
            LogLevel.DEBUG -> android.util.Log.d("${tag ?: ""}", "$message")
            LogLevel.INFO -> android.util.Log.i("${tag ?: ""}", "$message")
            LogLevel.WARNING -> android.util.Log.w("${tag ?: ""}", "$message")
            LogLevel.EXCEPTION -> android.util.Log.e("${tag ?: ""}", "$message", throwable)
        }.asUnit()
    }
}