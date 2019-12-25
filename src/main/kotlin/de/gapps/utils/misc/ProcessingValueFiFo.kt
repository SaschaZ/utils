package de.gapps.utils.misc

import de.gapps.utils.coroutines.channel.IProcessValue


open class ProcessingValueFiFo<T>(amount: Int) : FiFo<IProcessValue<T>>(amount) {

    open fun putValue(value: IProcessValue<T>) = put(value, value.time == values.lastOrNull()?.time)
}

