package de.gapps.utils.ta.output.chart

import de.gapps.utils.misc.Vec2d
import de.gapps.utils.ta.input.IChartRequest


data class SingleChart(
    override val request: IChartRequest,
    override val chartInput: ChartInput,
    override val size: Vec2d,
    override val title: ChartTitle,
    override val textSize: Double,
    override val margin: ChartMargin,
    override val lineWidth: Int,
    override val visibleCandleCount: Int,
    override val visibleSideOffsets: Pair<Int, Int>,
    override val xTicsMinutes: Int
) : IChart<IChartRequest, ChartInput>