package de.gapps.utils.time

import de.gapps.utils.time.base.IMillisecondHolder
import de.gapps.utils.time.base.TimeUnit
import de.gapps.utils.time.base.TimeUnit.MS
import de.gapps.utils.time.base.toMillis
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*
import kotlin.reflect.KClass

open class TimeEx(override val millis: Long = Date().time.toMillis(MS)) : ITimeEx {

    companion object : TimeParseHelper()

    constructor(value: Number, timeUnit: TimeUnit = MS) : this(value.toLong().toMillis(timeUnit))
    constructor(source: String) : this(source.stringToMillis())
    constructor(zdt: ZonedDateTime) : this(zdt.toInstant().toEpochMilli())
    constructor(ldt: LocalDateTime) :
            this(ldt.toInstant(ZoneOffset.systemDefault().rules.getOffset(ldt)).toEpochMilli())

    override val clazz: KClass<TimeEx> = TimeEx::class

    override fun toString() = formatTime(StringConverter.DateFormat.COMPLETE)
    override fun equals(other: Any?) = millis == (other as? ITimeEx)?.millis
    override fun hashCode() = millis.hashCode() + javaClass.hashCode()
}


fun Number.toTime() = toTime(MS)
infix fun Number.toTime(unit: TimeUnit) = TimeEx(this, unit)

val IMillisecondHolder.time: ITimeEx
    get() = millis.toTime()

fun LocalDateTime.toTime() = TimeEx(this)
fun ZonedDateTime.toTime() = TimeEx(this)