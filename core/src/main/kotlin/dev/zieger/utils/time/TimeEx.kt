package dev.zieger.utils.time

import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.TimeUnit.MS
import dev.zieger.utils.time.base.toMillis
import dev.zieger.utils.time.duration.IDurationHolder
import java.util.*
import kotlin.collections.HashMap

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

    @Suppress("ReplaceWithEnumMap")
    override val timeUnitLengthMap: HashMap<Long, FiFo<Int>> = HashMap()

    override fun toString() = formatTime(DateFormat.COMPLETE)
    override fun equals(other: Any?) = millis == (other as? ITimeEx)?.millis
            && zone == other.zone

    override fun hashCode() = millis.hashCode() + zone.hashCode() + javaClass.hashCode()
}


fun Number.toTime() = toTime(MS)
infix fun Number.toTime(unit: TimeUnit) = TimeEx(this, unit)

val IDurationHolder.time: ITimeEx
    get() = millis.toTime()