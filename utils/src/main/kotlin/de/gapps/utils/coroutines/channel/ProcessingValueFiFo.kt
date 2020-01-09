package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.channel.pipeline.IPipeValue
import de.gapps.utils.misc.FiFo
import de.gapps.utils.misc.lastOrNull


open class ProcessingValueFiFo<T>(amount: Int) : FiFo<IPipeValue<T>>(amount) {

    open fun putValue(value: IPipeValue<T>) = put(value, value.time == values.lastOrNull()?.time)
}

