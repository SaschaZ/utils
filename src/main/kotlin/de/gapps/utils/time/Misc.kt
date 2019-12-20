package de.gapps.utils.time

import de.gapps.utils.time.duration.IDurationEx


suspend fun delay(time: IDurationEx) = kotlinx.coroutines.delay(time.millis)

fun ClosedRange<ITimeEx>.startTimes(
    interval: IDurationEx,
    count: Int
): TimeExProgression = (start..endInclusive step interval * count)
