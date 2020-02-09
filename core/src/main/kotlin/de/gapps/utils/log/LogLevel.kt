package de.gapps.utils.log

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    EXCEPTION;

    val short: String
        get() = name[0].toString()
}