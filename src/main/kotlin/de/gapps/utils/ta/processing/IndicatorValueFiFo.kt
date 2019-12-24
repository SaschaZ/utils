package de.gapps.utils.ta.processing

import de.gapps.utils.misc.FiFo
import de.gapps.utils.misc.lastOrNull
import de.gapps.utils.ta.input.IIndicatorVal
import de.gapps.utils.time.time

open class IndicatorValueFiFo<T>(amount: Int) : FiFo<IIndicatorVal<T>>(amount) {

    open fun putValue(value: IIndicatorVal<T>) = put(value, value.time == values.lastOrNull()?.time)
}