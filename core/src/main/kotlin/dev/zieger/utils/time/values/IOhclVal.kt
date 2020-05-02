package dev.zieger.utils.time.values

import dev.zieger.utils.time.base.INanoTime

interface IOhclVal : INanoTime {

    val open: Double
    val high: Double
    val close: Double
    val low: Double
    val volume: Long
    val partial: Boolean
}

