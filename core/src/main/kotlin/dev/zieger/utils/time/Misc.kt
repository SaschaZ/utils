package dev.zieger.utils.time

import dev.zieger.utils.time.base.times
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.progression.DurationExProgression
import dev.zieger.utils.time.progression.TimeExProgression
import dev.zieger.utils.time.progression.step


suspend fun delay(time: IDurationEx) = kotlinx.coroutines.delay(time.millis)

fun ClosedRange<ITimeEx>.ticks(
    interval: IDurationEx,
    count: Int = 1
): TimeExProgression = start..endInclusive step interval * count

fun ClosedRange<IDurationEx>.ticks(
    interval: IDurationEx,
    count: Int = 1
): DurationExProgression = start..endInclusive step interval * count