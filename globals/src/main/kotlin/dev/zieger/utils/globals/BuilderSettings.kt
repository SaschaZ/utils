package dev.zieger.utils.globals

import java.io.File

object BuilderSettings {

    var PRINT_EXCEPTIONS = false
    var LOG_EXCEPTIONS = false
    var ERROR_LOG_FILE: () -> File = { File("error.log") }
}