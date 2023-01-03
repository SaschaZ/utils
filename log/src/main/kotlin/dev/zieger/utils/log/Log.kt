package dev.zieger.utils.log

/**
 * Global [ILogContext].
 * Is hold by the global [LogScope] property.
 */
val Log
    get() = LogScope.Log

/**
 * Global [ILogScope].
 */
var LogScope: ILogScope = LogScopeImpl()
    internal set