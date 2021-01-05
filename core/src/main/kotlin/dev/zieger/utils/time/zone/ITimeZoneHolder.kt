package dev.zieger.utils.time.zone

import dev.zieger.utils.time.TimeEx
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

interface ITimeZoneHolder {
    val zone: TimeZone
}

val UTC: TimeZone = TimeZone.getTimeZone(ZoneId.ofOffset("UTC", ZoneOffset.UTC))

open class UtcTime(millis: Long = System.currentTimeMillis()) : TimeEx(millis, UTC)