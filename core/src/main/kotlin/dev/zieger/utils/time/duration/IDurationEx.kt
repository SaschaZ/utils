package dev.zieger.utils.time.duration

import dev.zieger.utils.time.base.INanoTime
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.bigD
import dev.zieger.utils.time.base.bigI

interface IDurationEx : INanoTime {

    fun formatDuration(): String {
        var nanosTmp = nanos.bigD
        return TimeUnit.values().filter { !it.isCloned }.sortedByDescending { it.factorNanos }.mapNotNull { unit ->
            val (div, mod) = nanosTmp.divMod(unit.factorNanos * 10.bigD.pow(9))
            nanosTmp = mod.bigD
            when {
                unit.exponent >= 0 && div.bigI > 0.bigI -> "${"%d".format(div.toLong())}${unit.shortChar}"
                unit.exponent < 0 && div.bigI > 0.bigI ->
                    "${"%d".format(div.toLong())}${unit.shortChar}"
                else -> null
            }
        }.joinToString(" ")
    }
}

