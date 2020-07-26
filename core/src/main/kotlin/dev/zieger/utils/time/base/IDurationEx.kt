package dev.zieger.utils.time.base

import dev.zieger.utils.misc.divMod

interface IDurationEx : IDurationHolderComparator {

    fun formatDuration(
        vararg entities: TimeUnit = TimeUnit.values(),
        maxEntities: Int = TimeUnit.values().size,
        sameLength: Boolean = false
    ): String {
        var entityCnt = 0
        var millisTmp = millis
        return entities.sortedByDescending { it.factorMillis }.mapNotNull { unit ->
            val factorMillis = unit.factorMillis
            val (div, mod) = millisTmp.divMod(factorMillis)
            millisTmp = mod
            if (div > 0L && entityCnt < maxEntities) {
                entityCnt++
                "${"%${if (sameLength) "3" else ""}d".format(div)}${unit.shortChar}"
            } else null
        }.joinToString(" ")
    }
}
