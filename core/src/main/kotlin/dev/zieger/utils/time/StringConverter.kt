package dev.zieger.utils.time

import dev.zieger.utils.misc.formatQuery
import dev.zieger.utils.time.DateFormat.*
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


enum class DateFormat {

    COMPLETE,
    DATE_ONLY,
    TIME_ONLY,
    HuM,
    PLOT,
    FILENAME,
    EXCHANGE
}

interface StringConverter : ZoneDateTimeHolder {

    fun formatTime(format: DateFormat = COMPLETE): String =
        StringConverterDelegate.formatTime(format, zoneDateTime, localDateTime)
}

object StringConverterDelegate {

    var formatTime: (DateFormat, ZonedDateTime, LocalDateTime) -> String = { format, zoneDateTime, localDateTime ->
        val formatter = DateTimeFormatter.ofPattern(
            when (format) {
                COMPLETE -> "dd.MM.yyyy-HH:mm:ss"
                DATE_ONLY -> "dd.MM.yyyy"
                TIME_ONLY -> "HH:mm:ss"
                HuM -> "HH:mm"
                PLOT -> "yyyy-MM-dd HH:mm:ss"
                FILENAME -> "yyyy-MM-dd-HH-mm-ss"
                EXCHANGE -> "yyyy-MM-dd HH:mm:ss"
            }
        )

        if (format == EXCHANGE) {
            zoneDateTime.withZoneSameInstant(TimeZone.getTimeZone("UTC").toZoneId()).format(formatter).formatQuery()
        } else localDateTime.format(formatter)!!
    }
}