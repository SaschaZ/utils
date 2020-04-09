package de.gapps.utils.time

import de.gapps.utils.time.base.IMillisecondHolder
import de.gapps.utils.time.base.TimeUnit
import de.gapps.utils.time.base.TimeUnit.MS
import de.gapps.utils.time.base.toMillis
import java.util.*

open class TimeEx(
    override val millis: Long = System.currentTimeMillis(),
    override val zone: TimeZone = TimeZone.getDefault()
) : ITimeEx {

    companion object : TimeParseHelper()

    constructor(value: Number, timeUnit: TimeUnit = MS, timeZone: TimeZone = TimeZone.getDefault()) :
            this(value.toLong().toMillis(timeUnit), timeZone)

    constructor(source: String, timeZone: TimeZone = TimeZone.getDefault()) :
            this(source.stringToMillis(timeZone), timeZone)

    constructor(date: Date, timeZone: TimeZone = TimeZone.getDefault()) :
            this(date.time, timeZone)

    override fun toString() = formatTime(DateFormat.COMPLETE)
    override fun equals(other: Any?) = millis == (other as? ITimeEx)?.millis && zone == other.zone
    override fun hashCode() = millis.hashCode() + zone.hashCode() + javaClass.hashCode()
}


fun Number.toTime() = toTime(MS)
infix fun Number.toTime(unit: TimeUnit) = TimeEx(this, unit)

val IMillisecondHolder.time: ITimeEx
    get() = millis.toTime()