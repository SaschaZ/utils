package dev.zieger.utils.coroutines.channel

import dev.zieger.utils.coroutines.channel.network.INodeValue
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.lastOrNull


open class ProcessingValueFiFo<T>(amount: Int) : FiFo<INodeValue<T>>(amount) {

    open fun putValue(value: INodeValue<T>) = put(value, value.time == values.lastOrNull()?.time)
}

