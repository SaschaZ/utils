package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.channel.network.INodeValue
import de.gapps.utils.misc.FiFo
import de.gapps.utils.misc.lastOrNull


open class ProcessingValueFiFo<T>(amount: Int) : FiFo<INodeValue<T>>(amount) {

    open fun putValue(value: INodeValue<T>) = put(value, value.time == values.lastOrNull()?.time)
}

