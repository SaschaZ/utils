package dev.zieger.utils.time.duration

import dev.zieger.utils.misc.castSafe
import dev.zieger.utils.time.TimeParseHelper
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.base.TimeUnit.*
import dev.zieger.utils.time.base.toMillis
import java.util.concurrent.atomic.AtomicLong

open class DurationEx private constructor(
    override val millis: Long = 0L
) : IDurationEx {

    companion object : TimeParseHelper() {
        private var lastId = AtomicLong(-1L)
        private val newId get() = lastId.incrementAndGet()
    }

    constructor(value: Number, timeUnit: TimeUnit = MILLI) : this(value.toLong().toMillis(timeUnit))

    override val id: Long = newId

    override fun toString() = formatDuration()
    override fun equals(other: Any?) =
        other?.castSafe<IDurationEx>()?.let { millis == it.millis } == true

    override fun hashCode() = millis.hashCode() + javaClass.hashCode()
}

fun Number.toDuration() = toDuration(MILLI)
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