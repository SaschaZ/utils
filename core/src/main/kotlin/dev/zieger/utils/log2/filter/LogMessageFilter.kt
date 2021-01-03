package dev.zieger.utils.log2.filter

import dev.zieger.utils.log2.IFilter
import dev.zieger.utils.log2.LogFilter
import dev.zieger.utils.log2.LogPipelineContext

data class LogMessageFilter(val regex: Regex) : LogFilter.LogPreFilter() {

    override fun call(context: LogPipelineContext, next: IFilter<LogPipelineContext>) {
        if (regex.matches(context.message.toString())) next(context)
        else context.cancel()
    }

    override fun copy() = LogMessageFilter(regex)
}