package dev.zieger.utils.time.duration

import dev.zieger.utils.misc.divMod
import dev.zieger.utils.time.base.IMillisecondHolderComparator
import dev.zieger.utils.time.base.TimeUnit

interface IDurationEx : IMillisecondHolderComparator {

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

