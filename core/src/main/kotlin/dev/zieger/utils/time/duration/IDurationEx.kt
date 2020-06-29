package dev.zieger.utils.time.duration

import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.divMod
import dev.zieger.utils.time.base.TimeUnit

interface IDurationEx : IDurationHolderComparator {

    val timeUnitLengthMap: HashMap<TimeUnit, FiFo<Int>>

    fun formatDuration(
        vararg entities: TimeUnit = TimeUnit.values()
    ): String {
        var millisTmp = millis
        return entities.sortedByDescending { it.factorMillis }.mapNotNull { unit ->
            val (div, mod) = millisTmp.divMod(unit.factorMillis)
            millisTmp = mod
            val amount = "%d".format(div)
            timeUnitLengthMap.getOrPut(unit) { FiFo(10) }.put(amount.length)
            if (div > 0L) "${"%${timeUnitLengthMap[unit]!!.max()}d".format(div)}${unit.shortChar}" else null
        }.joinToString(" ")
    }
}

