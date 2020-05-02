package dev.zieger.utils.time.duration

import dev.zieger.utils.misc.divMod
import dev.zieger.utils.time.base.INanoTime
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.bigD
import dev.zieger.utils.time.base.bigI

interface IDurationEx : INanoTime {

    fun formatDuration(): String {
        var secondsTmp = seconds
        return TimeUnit.values().filter { !it.isCloned }.sortedByDescending { it.factorNanos }.mapNotNull { unit ->
            val (div, mod) = secondsTmp.bigD.divMod(unit.factorNanos)
            secondsTmp = mod.bigI
            if (div.bigI > 0.bigI) "${"%d".format(div.toLong())}${unit.shortChar}" else null
        }.joinToString(" ")
    }
}

