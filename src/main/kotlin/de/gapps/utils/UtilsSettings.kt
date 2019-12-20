package de.gapps.utils

import de.gapps.utils.misc.Log
import java.io.File

object UtilsSettings {

    var LOG_LEVEL: Log.LogLevel = Log.LogLevel.VERBOSE

    var PRINT_EXCEPTIONS = true
    var LOG_EXCEPTIONS = true
    var ERROR_LOG_FILE: () -> File? = { File("errorLog.txt") }
}