@file:Suppress("unused")

package dev.zieger.utils.coroutines.channel

import dev.zieger.utils.coroutines.channel.pipeline.IPipeValue
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.lastOrNull


open class PipeValueFiFo<T>(amount: Int) : FiFo<IPipeValue<T>>(amount) {

    open fun putValue(value: IPipeValue<T>) = put(value, value.time == values.lastOrNull()?.time)
}

