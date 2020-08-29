package dev.zieger.utils.log2.calls

import dev.zieger.utils.log2.LogPipelineContext

interface IInlineLogBuilder {
    infix fun <T> T.logV(msg: LogPipelineContext.(T) -> Any): T
    infix fun <T> T.logD(msg: LogPipelineContext.(T) -> Any): T
    infix fun <T> T.logI(msg: LogPipelineContext.(T) -> Any): T
    infix fun <T> T.logW(msg: LogPipelineContext.(T) -> Any): T
    infix fun <T> T.logE(msg: LogPipelineContext.(T) -> Any): T
}