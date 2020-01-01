package de.gapps.utils.misc

import de.gapps.utils.coroutines.channel.network.INodeValue


open class ProcessingValueFiFo<T>(amount: Int) : FiFo<INodeValue<T>>(amount) {

    open fun putValue(value: INodeValue<T>) = put(value, value.time == values.lastOrNull()?.time)
}

