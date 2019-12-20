package de.gapps.utils.time.values

import de.gapps.utils.time.base.IMillisecondHolder

interface IOhclVal : IMillisecondHolder {

    val open: Double
    val high: Double
    val close: Double
    val low: Double
    val volume: Long
}

