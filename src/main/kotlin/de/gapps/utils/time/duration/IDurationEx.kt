package de.gapps.utils.time.duration

import de.gapps.utils.misc.divMod
import de.gapps.utils.time.base.IMillisecondArithmetic
import de.gapps.utils.time.base.TimeUnit

interface IDurationEx : IMillisecondArithmetic<IDurationEx, DurationEx> {

    fun formatDuration(
        align: Boolean = false,
        onlyDaysAndHours: Boolean = false,
        withSeconds: Boolean = false
    ): String {
        var millisTmp = millis
        return TimeUnit.values().filter { !it.isCloned }.sortedByDescending { it.factorMillis }.mapNotNull { unit ->
            val (div, mod) = millisTmp.divMod(unit.factorMillis)
            millisTmp = mod
            if (div > 0L) "${"%d".format(div)}${unit.shortChar}" else null
        }.joinToString(" ")
    }
}

