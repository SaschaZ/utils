package dev.zieger.utils.log

val Log
    get() = LogScope.Log

var LogScope: ILogScope = LogScopeImpl()
    internal set