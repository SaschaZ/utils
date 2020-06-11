package dev.zieger.utils.log

/**
 * Log-Settings
 */
interface ILogSettings {
    /**
     * Minimum [LogLevel] to print.
     * Is used by [LogLevelElement] that is added to every [LogContext] by default.
     */
    var minLogLevel: LogLevel

    fun ILogSettings.copy(
        level: LogLevel = minLogLevel
    ): ILogSettings = LogSettings(level)
}

open class LogSettings(
    override var minLogLevel: LogLevel = LogLevel.VERBOSE
) : ILogSettings