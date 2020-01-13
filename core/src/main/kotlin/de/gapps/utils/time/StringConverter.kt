package de.gapps.utils.time

import de.gapps.utils.misc.formatQuery
import de.gapps.utils.time.StringConverter.DateFormat.*
import java.time.format.DateTimeFormatter
import java.util.*

interface StringConverter : ZoneDateTimeHolder {

    enum class DateFormat {

        COMPLETE,
        DATE_ONLY,
        TIME_ONLY,
        HuM,
        PLOT,
        FILENAME,
        EXCHANGE
    }

    fun formatTime(format: DateFormat = COMPLETE): String {
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

        return if (format == EXCHANGE) {
            zoneDateTime.withZoneSameInstant(TimeZone.getTimeZone("UTC").toZoneId()).format(formatter).formatQuery()
        } else localDateTime.format(formatter)!!
    }
}