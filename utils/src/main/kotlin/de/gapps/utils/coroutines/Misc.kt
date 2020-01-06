@file:Suppress("unused")

package de.gapps.utils.coroutines

import de.gapps.utils.UtilsSettings.ERROR_LOG_FILE
import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.misc.readString
import de.gapps.utils.misc.writeString
import de.gapps.utils.time.TimeEx
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
