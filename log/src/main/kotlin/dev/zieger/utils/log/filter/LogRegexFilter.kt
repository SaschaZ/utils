package dev.zieger.utils.log.filter

import dev.zieger.utils.log.IFilter
import dev.zieger.utils.log.ILogQueueContext
import dev.zieger.utils.log.LogFilter

data class LogRegexFilter(val regex: Regex) : LogFilter.LogPreFilter() {

    override fun call(context: ILogQueueContext, next: IFilter<ILogQueueContext>) {
        if (regex.matches(context.message.toString())) next(context)
        else context.cancel()
    }

    override fun copy() = LogRegexFilter(regex)
}