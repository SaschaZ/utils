package de.gapps.utils.time

import de.gapps.utils.time.base.plus
import de.gapps.utils.time.base.times
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.duration.milliseconds
import de.gapps.utils.time.progression.DurationExProgression
import de.gapps.utils.time.progression.TimeExProgression
import de.gapps.utils.time.progression.step


suspend fun delay(time: IDurationEx) = kotlinx.coroutines.delay(time.millis)

fun ClosedRange<ITimeEx>.ticks(
    interval: IDurationEx,
    count: Int = 1
): TimeExProgression = start..endInclusive step interval * count

fun ClosedRange<IDurationEx>.ticks(
    interval: IDurationEx,
    count: Int = 1
): DurationExProgression = start..endInclusive step interval * count