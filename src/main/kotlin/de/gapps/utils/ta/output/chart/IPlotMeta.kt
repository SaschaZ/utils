package de.gapps.utils.ta.output.chart

interface IPlotMeta {

    val hasValues: Boolean
    val yMin: Number
    val yMax: Number
    val yTicMin: Number
    val yTicMax: Number
    val yTicStepSize: Number
}