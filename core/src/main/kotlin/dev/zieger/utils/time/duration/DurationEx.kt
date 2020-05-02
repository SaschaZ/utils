@file:Suppress("unused")

package dev.zieger.utils.time.duration

import dev.zieger.utils.time.TimeParseHelper
import dev.zieger.utils.time.base.*
import dev.zieger.utils.time.base.TimeUnit.*
import java.math.BigInteger

open class DurationEx(
    nanos: BigInteger
) : NanoTime(nanos), IDurationEx {

    companion object : TimeParseHelper()

    constructor(millis: Long = 0, nanos: Long = System.nanoTime()) : this((millis to nanos).big)
    constructor(value: BigInteger, unit: TimeUnit) : this(value.toNanos(unit))

    override fun toString() = formatDuration()
}

fun BigInteger.toDuration(): IDurationEx = toDuration(N)
fun INanoTime.toDuration(): IDurationEx = toDuration(N)
fun Number.toDuration(): IDurationEx = toDuration(MS)

infix fun BigInteger.toDuration(unit: TimeUnit): IDurationEx = DurationEx(this, unit)
infix fun Number.toDuration(unit: TimeUnit): IDurationEx = bigI.toDuration(unit)
infix fun INanoTime.toDuration(unit: TimeUnit): IDurationEx = nanosInternal.toDuration(unit)


val BigInteger.nanos: IDurationEx
    get() = toDuration(NANOS)
val Number.nanos: IDurationEx
    get() = toDuration(NANOS)
val BigInteger.micros: IDurationEx
    get() = toDuration(MICROS)
val Number.micros: IDurationEx
    get() = toDuration(MICROS)
val BigInteger.milliseconds: IDurationEx
    get() = toDuration(MILLI)
val Number.milliseconds: IDurationEx
    get() = toDuration(MILLI)
val BigInteger.seconds: IDurationEx
    get() = toDuration(SECOND)
val Number.seconds: IDurationEx
    get() = toDuration(SECOND)
val BigInteger.minutes: IDurationEx
    get() = toDuration(MINUTE)
val Number.minutes: IDurationEx
    get() = toDuration(MINUTE)
val BigInteger.hours: IDurationEx
    get() = toDuration(HOUR)
val Number.hours: IDurationEx
    get() = toDuration(HOUR)
val BigInteger.days: IDurationEx
    get() = toDuration(DAY)
val Number.days: IDurationEx
    get() = toDuration(DAY)
val BigInteger.weeks: IDurationEx
    get() = toDuration(WEEK)
val Number.weeks: IDurationEx
    get() = toDuration(WEEK)
val BigInteger.months: IDurationEx
    get() = toDuration(MONTH)
val Number.months: IDurationEx
    get() = toDuration(MONTH)
val BigInteger.years: IDurationEx
    get() = toDuration(YEAR)
val Number.years: IDurationEx
    get() = toDuration(YEAR)