package dev.zieger.utils.time

import dev.zieger.utils.misc.castSafe
import dev.zieger.utils.time.base.INanoTime
import dev.zieger.utils.time.base.NanoTime
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.TimeUnit.MS
import dev.zieger.utils.time.base.toNanos
import java.math.BigInteger
import java.util.*

open class TimeEx(
    nanos: BigInteger,
    override val zone: TimeZone = TimeZone.getDefault()
) : NanoTime(nanos), ITimeEx {

    companion object : TimeParseHelper()

    constructor(millis: Long = System.currentTimeMillis(), nanos: Long = System.nanoTime()) :
            this((millis to nanos).big)

    constructor(value: Number, timeUnit: TimeUnit = MS, timeZone: TimeZone = TimeZone.getDefault()) :
            this(value.toNanos(timeUnit), timeZone)

    constructor(source: String, timeZone: TimeZone = TimeZone.getDefault()) :
            this(source.stringToMillis(timeZone), MS, timeZone)

    constructor(date: Date, timeZone: TimeZone = TimeZone.getDefault()) :
            this(date.time, MS, timeZone)

    override fun toString() = formatTime(DateFormat.COMPLETE)
    override fun equals(other: Any?) = super.equals(other)
            && zone == other.castSafe<ITimeEx>()?.zone

    override fun hashCode() = super.hashCode() + zone.hashCode()
}


fun Number.toTime() = toTime(MS)
infix fun Number.toTime(unit: TimeUnit) = TimeEx(this, unit)

val INanoTime.time: ITimeEx
    get() = millis.toTime()