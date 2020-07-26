package dev.zieger.utils.time.zone

import dev.zieger.utils.time.base.IDurationHolder
import dev.zieger.utils.time.toTime
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

interface LocalDateTimeHolder : IDurationHolder {

    val localDateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(millis.toTime().millis),
            TimeZone.getDefault().toZoneId()
        )!!
}