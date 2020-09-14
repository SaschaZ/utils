@file:Suppress("unused")

package dev.zieger.utils.coroutines

import dev.zieger.utils.UtilsSettings.ERROR_LOG_FILE
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.readString
import dev.zieger.utils.misc.writeString
import dev.zieger.utils.time.TimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

private val errorLogMutex = Mutex()

fun CoroutineScope.logToFile(throwable: Throwable) =
    launchEx(mutex = errorLogMutex) {
        ERROR_LOG_FILE()?.also { file ->
            var log = "${TimeEx().formatTime()}: ${javaClass.simpleName}: $throwable.message\n"
            log += throwable.stackTrace.joinToString("\n")
            log += "\n\n\n"
            log += file.readString()
            file.writeString(log)
        }
    }
