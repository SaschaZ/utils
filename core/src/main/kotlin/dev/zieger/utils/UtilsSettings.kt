package dev.zieger.utils

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogLevel
import kotlinx.coroutines.CoroutineScope
import java.io.File

object UtilsSettings {

    var LOG_LEVEL: LogLevel
        get() = Log.logLevel
        set(value) {
            Log.logLevel = value
        }

    var PRINT_EXCEPTIONS = true
    var LOG_EXCEPTIONS = false
    var LOG_SCOPE: CoroutineScope = DefaultCoroutineScope()
    var ERROR_LOG_FILE: () -> File? = { null }
}