package dev.zieger.utils.time.duration

import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.divMod
import dev.zieger.utils.time.base.TimeUnit

interface IDurationEx : IDurationHolderComparator {

    val timeUnitLengthMap: HashMap<Long, FiFo<Int>>

    fun formatDuration(
        vararg entities: TimeUnit = TimeUnit.values()
    ): String {
        var millisTmp = millis
        return entities.sortedByDescending { it.factorMillis }.mapNotNull { unit ->
            val factorMillis = unit.factorMillis
            val (div, mod) = millisTmp.divMod(factorMillis)
            millisTmp = mod
            if (div > 0L) {
                timeUnitLengthMap.getOrPut(factorMillis) { FiFo(10) }.put("%d".format(div).length)
                "${"%${timeUnitLengthMap[factorMillis]!!.max()}d".format(div)}${unit.shortChar}"
            } else null
        }.joinToString(" ")
    }
}

