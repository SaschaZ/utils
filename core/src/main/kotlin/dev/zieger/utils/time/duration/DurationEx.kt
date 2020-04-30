package dev.zieger.utils.time.duration

import dev.zieger.utils.misc.castSafe
import dev.zieger.utils.time.TimeParseHelper
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.TimeUnit.*
import dev.zieger.utils.time.base.toMillis

open class DurationEx private constructor(
    override val millis: Long = 0L,
    override val nanos: Long = 0L
) : IDurationEx {

    companion object : TimeParseHelper()

    constructor(value: Number, timeUnit: TimeUnit = MS) : this(value.toLong().toMillis(timeUnit))

    override fun toString() = formatDuration()
    override fun equals(other: Any?) =
        other?.castSafe<IDurationEx>()?.let { millis == it.millis && nanos == it.nanos } == true

    override fun hashCode() = millis.hashCode() + nanos.hashCode() + javaClass.hashCode()
}

fun Number.toDuration() = toDuration(MS)
infix fun Number.toDuration(unit: TimeUnit) = DurationEx(toLong(), unit)
fun IDurationHolder.toDuration(): IDurationEx = DurationEx(millis)


val Number.milliseconds: IDurationEx
    get() = toDuration(MILLI)
val Number.seconds: IDurationEx
    get() = toDuration(SECOND)
val Number.minutes: IDurationEx
    get() = toDuration(MINUTE)
val Number.hours: IDurationEx
    get() = toDuration(HOUR)
val Number.days: IDurationEx
    get() = toDuration(DAY)
val Number.weeks: IDurationEx
    get() = toDuration(WEEK)
val Number.months: IDurationEx
    get() = toDuration(MONTH)
val Number.years: IDurationEx
    get() = toDuration(YEAR)