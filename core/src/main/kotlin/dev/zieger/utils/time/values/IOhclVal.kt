package dev.zieger.utils.time.values

import dev.zieger.utils.time.base.IMillisecondHolder

interface IOhclVal : IMillisecondHolder {

    val open: Double
    val high: Double
    val close: Double
    val low: Double
    val volume: Long
    val partial: Boolean
}

