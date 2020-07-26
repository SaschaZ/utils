package dev.zieger.utils.time.zone

import java.time.ZonedDateTime
import java.util.*

interface ZoneDateTimeHolder : LocalDateTimeHolder {

    val zoneDateTime: ZonedDateTime
        get() = ZonedDateTime.of(localDateTime, TimeZone.getDefault().toZoneId())
}