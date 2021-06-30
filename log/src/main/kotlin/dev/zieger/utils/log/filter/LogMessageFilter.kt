package dev.zieger.utils.log.filter

import dev.zieger.utils.log.IFilter
import dev.zieger.utils.log.LogFilter
import dev.zieger.utils.log.LogPipelineContext

data class LogMessageFilter(val regex: Regex) : LogFilter.LogPreFilter() {

    override fun call(context: LogPipelineContext, next: IFilter<LogPipelineContext>) {
        if (regex.matches(context.message.toString())) next(context)
        else context.cancel()
    }

    override fun copy() = LogMessageFilter(regex)
}