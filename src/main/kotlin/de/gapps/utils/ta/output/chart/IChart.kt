package de.gapps.utils.ta.output.chart

import de.gapps.utils.misc.Vec2d
import de.gapps.utils.ta.input.IChartRequest
import de.gapps.utils.time.base.TimeUnit.MINUTE
import de.gapps.utils.time.duration.toDuration

interface IChart<out R : IChartRequest, out P : IChartInput> {


    val request: R
    val chartInput: P

    val size: Vec2d
    val title: ChartTitle
    val textSize: Double
    val margin: ChartMargin
    val lineWidth: Int
    val visibleCandleCount: Int
    val visibleSideOffsets: Pair<Int, Int>
    val xTicsMinutes: Int
}

val IChart<*, *>.doubleLineWidth: Int
    get() = lineWidth * 2
val IChart<*, *>.xTics: Int
    get() = xTicsMinutes.toDuration(MINUTE).millis.toInt()