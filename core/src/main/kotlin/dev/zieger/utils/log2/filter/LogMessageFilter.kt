package dev.zieger.utils.log2.filter

import dev.zieger.utils.log2.IHook
import dev.zieger.utils.log2.LogHook
import dev.zieger.utils.log2.LogPipelineContext

data class LogMessageFilter(val regex: Regex) : LogHook.LogPreHook() {

    override fun LogPipelineContext.call(next: IHook<LogPipelineContext>) {
        if (regex.matches(message.toString())) next(this)
        else cancel()
    }

    override fun copy() = this
}