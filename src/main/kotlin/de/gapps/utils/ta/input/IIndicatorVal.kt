package de.gapps.utils.ta.input

import de.gapps.utils.ta.input.config.IPlotConfig
import de.gapps.utils.time.values.ITimeVal


interface IIndicatorVal<out T : Any?> : ITimeVal<T> {

    val plotConfig: IPlotConfig<*, *>
}