package dev.zieger.utils.time.duration

import dev.zieger.utils.time.TimeParseHelper
import dev.zieger.utils.time.base.IMillisecondHolder
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.TimeUnit.MS
import dev.zieger.utils.time.base.toMillis

open class DurationEx private constructor(override val millis: Long = 0L) : IDurationEx {

    companion object : TimeParseHelper()

    constructor(value: Number, timeUnit: TimeUnit = MS) : this(value.toLong().toMillis(timeUnit))

    override fun toString() = formatDuration()
    override fun equals(other: Any?) = millis == (other as? IDurationEx)?.millis
    override fun hashCode() = millis.hashCode() + javaClass.hashCode()
}

fun Number.toDuration() = toDuration(MS)
infix fun Number.toDuration(unit: TimeUnit) = DurationEx(toLong(), unit)
fun IMillisecondHolder.toDuration(): IDurationEx = DurationEx(millis)


val Number.milliseconds: IDurationEx
    get() = toDuration(TimeUnit.MILLI)
val Number.seconds: IDurationEx
    get() = toDuration(TimeUnit.SECOND)
val Number.minutes: IDurationEx
    get() = toDuration(TimeUnit.MINUTE)
val Number.hours: IDurationEx
    get() = toDuration(TimeUnit.HOUR)
val Number.days: IDurationEx
    get() = toDuration(TimeUnit.DAY)
val Number.weeks: IDurationEx
    get() = toDuration(TimeUnit.WEEK)
val Number.months: IDurationEx
    get() = toDuration(TimeUnit.MONTH)
val Number.years: IDurationEx
    get() = toDuration(TimeUnit.YEAR)