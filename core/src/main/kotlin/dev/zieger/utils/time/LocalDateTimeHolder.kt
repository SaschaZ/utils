package dev.zieger.utils.time

import dev.zieger.utils.time.duration.IDurationHolder
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