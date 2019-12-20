package de.gapps.utils.time

import de.gapps.utils.time.base.IMillisecondHolder
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