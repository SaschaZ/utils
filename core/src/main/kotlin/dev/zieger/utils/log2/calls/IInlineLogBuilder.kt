package dev.zieger.utils.log2.calls

import dev.zieger.utils.log2.ILogMessageContext

interface IInlineLogBuilder {
    infix fun <T> T.logV(msg: ILogMessageContext.(T) -> Any): T
    infix fun <T> T.logD(msg: ILogMessageContext.(T) -> Any): T
    infix fun <T> T.logI(msg: ILogMessageContext.(T) -> Any): T
    infix fun <T> T.logW(msg: ILogMessageContext.(T) -> Any): T
    infix fun <T> T.logE(msg: ILogMessageContext.(T) -> Any): T
}