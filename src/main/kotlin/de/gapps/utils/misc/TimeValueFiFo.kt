package de.gapps.utils.misc

import de.gapps.utils.time.time
import de.gapps.utils.time.values.ITimeVal


open class TimeValueFiFo<T>(amount: Int) : FiFo<ITimeVal<T>>(amount) {

    open fun putValue(value: ITimeVal<T>) = put(value, value.time == values.lastOrNull()?.time)
}

