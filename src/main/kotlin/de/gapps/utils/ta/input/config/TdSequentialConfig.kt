package de.gapps.utils.ta.input.config

import de.gapps.utils.ta.output.EPlotLocation
import de.gapps.utils.ta.processing.SSeriesStyleType
import de.gapps.utils.time.time
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal
import de.gapps.utils.time.values.STimeVal

data class TdSequentialConfig<I : IOhclVal>(
    val useSlowSetupEntry: Boolean = false,
    val tdSmallTextSize: Int = 20,
    val tdBigTextSize: Int = 30,
    override val plotLocation: EPlotLocation = EPlotLocation.MAIN,
    override val seriesStyleType: SSeriesStyleType = SSeriesStyleType.TD(),
    override val idx: Int = 0,
    override val valueSelector: (ITimeVal<I>) -> ITimeVal<Double> = {
        STimeVal.TimeVal(
            it.value.close,
            it.time
        )
    }
) : IPlotConfig<I, Double>