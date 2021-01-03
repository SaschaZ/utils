package dev.zieger.utils.log2.filter

import dev.zieger.utils.log2.IFilter
import dev.zieger.utils.log2.ILogMessageContext
import dev.zieger.utils.log2.LogFilter
import dev.zieger.utils.log2.LogPipelineContext

class LogCondition(val condition: ILogMessageContext.() -> Boolean) : LogFilter.LogPreFilter() {

    override fun copy(): LogPreFilter = LogCondition(condition)

    override fun call(context: LogPipelineContext, next: IFilter<LogPipelineContext>) =
        if (condition(context)) next(context) else Unit
}