package dev.zieger.utils.time

import java.time.ZonedDateTime
import java.util.*

interface ZoneDateTimeHolder : LocalDateTimeHolder {

    val zoneDateTime: ZonedDateTime
        get() = ZonedDateTime.of(localDateTime, TimeZone.getDefault().toZoneId())
}