package de.gapps.utils.ta.output.chart

import de.gapps.utils.ta.input.IIndicatorVal
import de.gapps.utils.ta.processing.SSeriesStyleType
import de.gapps.utils.time.values.IOhclVal
import de.gapps.utils.time.values.ITimeVal
import de.gapps.utils.time.values.STimeVal

class PlotMeta(vararg values: List<ITimeVal<Any?>>) : IPlotMeta {

    private val valList = values.toList().flatten()

    @Suppress("UNCHECKED_CAST")
    private val nonNullValues: List<IIndicatorVal<Any>> =
        valList.filter { it.value != null } as List<IIndicatorVal<Any>>

    private val plotSeriesDef = nonNullValues.firstOrNull()?.plotConfig

    override val hasValues = valList.isNotEmpty()

    override val yMin: Number = when (plotSeriesDef?.seriesStyleType) {
        is SSeriesStyleType.CANDLE ->
            (valList.minBy { (it as IOhclVal).low } as? IOhclVal)?.low
        is SSeriesStyleType.SINGLE_LINE ->
            valList.minBy { it.value as Double }?.value as? Number
        is SSeriesStyleType.TRIPLE_LINE ->
            valList.minBy { (it.value as STimeVal.TripleLineValue).min() ?: Double.MAX_VALUE }?.value as? Number
        is SSeriesStyleType.IMPULSE ->
            valList.minBy { it.value as Double } as? Number
        else -> null
    } ?: Double.MAX_VALUE as Number

    override val yMax: Number = when (plotSeriesDef?.seriesStyleType) {
        is SSeriesStyleType.CANDLE ->
            (valList.maxBy { (it as IOhclVal).high } as? IOhclVal)?.high
        is SSeriesStyleType.SINGLE_LINE ->
            valList.maxBy { it.value as Double }?.value as? Number
        is SSeriesStyleType.TRIPLE_LINE ->
            valList.maxBy { (it.value as STimeVal.TripleLineValue).max() ?: Double.MIN_VALUE }?.value as? Number
        is SSeriesStyleType.IMPULSE ->
            valList.maxBy { it.value as Double } as? Number
        else -> null
    } ?: Double.MAX_VALUE as Number

    override val yTicMin: Number
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val yTicMax: Number
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val yTicStepSize: Number
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
}