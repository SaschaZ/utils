package dev.zieger.utils.log2.filter

import dev.zieger.utils.log2.IFilter
import dev.zieger.utils.log2.LogFilter
import dev.zieger.utils.log2.LogPipelineContext

data class LogMessageFilter(val regex: Regex) : LogFilter.LogPreFilter() {

    override fun LogPipelineContext.call(next: IFilter<LogPipelineContext>) {
        if (regex.matches(message.toString())) next(this)
        else cancel()
    }

    override fun copy() = LogMessageFilter(regex)
}