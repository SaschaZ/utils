package dev.zieger.utils.log2.filter

import dev.zieger.utils.log2.IFilter
import dev.zieger.utils.log2.ILogMessageContext
import dev.zieger.utils.log2.LogFilter
import dev.zieger.utils.log2.LogPipelineContext

class LogCondition(val condition: ILogMessageContext.() -> Boolean) : LogFilter.LogPreFilter() {

    override fun copy(): LogPreFilter = LogCondition(condition)

    override fun LogPipelineContext.call(next: IFilter<LogPipelineContext>) =
        if (condition()) next(this) else Unit
}