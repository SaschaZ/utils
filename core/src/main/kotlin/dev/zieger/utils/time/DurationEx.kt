package dev.zieger.utils.time

import dev.zieger.utils.misc.INumber
import dev.zieger.utils.misc.NumberEx
import dev.zieger.utils.misc.castSafe
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.IDurationHolder
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.TimeUnit.*
import dev.zieger.utils.time.base.toMillis
import dev.zieger.utils.time.string.TimeParseHelper

open class DurationEx private constructor(
    override val millis: Long = 0L
) : INumber by NumberEx(millis), IDurationEx {

    companion object : TimeParseHelper()

    constructor(value: Number, timeUnit: TimeUnit = MS) : this(value.toLong().toMillis(timeUnit))

    override fun toString() = formatDuration()
    override fun equals(other: Any?) =
        other?.castSafe<IDurationEx>()?.let { millis == it.millis } == true

    override fun hashCode() = millis.hashCode() + javaClass.hashCode()
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