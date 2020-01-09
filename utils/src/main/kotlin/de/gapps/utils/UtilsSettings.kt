package de.gapps.utils

import de.gapps.utils.log.LogLevel
import java.io.File

object UtilsSettings {

    var LOG_LEVEL: LogLevel = LogLevel.VERBOSE

    var PRINT_EXCEPTIONS = true
    var LOG_EXCEPTIONS = true
    var ERROR_LOG_FILE: () -> File? = { File("error.log") }
}