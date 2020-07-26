package dev.zieger.utils

import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogLevel
import java.io.File

object UtilsSettings {

    var LOG_LEVEL: LogLevel
        get() = Log.logLevel
        set(value) {
            Log.logLevel = value
        }

    var PRINT_EXCEPTIONS = true
    var LOG_EXCEPTIONS = true
    var ERROR_LOG_FILE: () -> File? = { File("error.log") }
}