package dev.zieger.utils.time

import dev.zieger.utils.time.base.INanoTime
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

interface LocalDateTimeHolder : INanoTime {

    val localDateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(millis.toLong().toTime().millis.toLong()),
            TimeZone.getDefault().toZoneId()
        )!!
}