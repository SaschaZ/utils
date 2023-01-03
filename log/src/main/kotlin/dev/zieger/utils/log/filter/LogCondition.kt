package dev.zieger.utils.log.filter

import dev.zieger.utils.log.IFilter
import dev.zieger.utils.log.ILogQueueContext
import dev.zieger.utils.log.LogFilter

class LogCondition(val condition: ILogQueueContext.() -> Boolean) : LogFilter.LogPreFilter() {

    override fun copy(): LogPreFilter = LogCondition(condition)

    override fun call(context: ILogQueueContext, next: IFilter<ILogQueueContext>) =
        if (condition(context)) next(context) else Unit
}