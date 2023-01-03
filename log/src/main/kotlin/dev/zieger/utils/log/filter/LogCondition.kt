package dev.zieger.utils.log.filter

import dev.zieger.utils.log.IFilter
import dev.zieger.utils.log.ILogMessageContext
import dev.zieger.utils.log.LogFilter

class LogCondition(val condition: ILogMessageContext.() -> Boolean) : LogFilter.LogPreFilter() {

    override fun copy(): LogPreFilter = LogCondition(condition)

    override fun call(context: ILogMessageContext, next: IFilter<ILogMessageContext>) =
        if (condition(context)) next(context) else context.cancel()
}