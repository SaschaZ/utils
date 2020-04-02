package dev.zieger.utils.time

import dev.zieger.utils.time.base.IMillisecondHolder
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

interface LocalDateTimeHolder : IMillisecondHolder {

    val localDateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(millis.toTime().millis),
            TimeZone.getDefault().toZoneId()
        )!!
}