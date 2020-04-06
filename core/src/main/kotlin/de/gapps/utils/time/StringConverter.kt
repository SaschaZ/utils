package de.gapps.utils.time

import de.gapps.utils.time.DateFormat.*
import de.gapps.utils.time.base.IMillisecondHolder
import de.gapps.utils.time.zone.ITimeZoneHolder
import java.text.SimpleDateFormat
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

interface StringConverter : IMillisecondHolder, ITimeZoneHolder {

    fun formatTime(format: DateFormat = COMPLETE): String =
        StringConverterDelegate.formatTime(format, millis, zone)
}

object StringConverterDelegate {

    var formatTime: (DateFormat, Long, TimeZone) -> String = { format, millis, zone ->
        val formatter = SimpleDateFormat(
            when (format) {
                COMPLETE -> "dd.MM.yyyy-HH:mm:ss"
                DATE_ONLY -> "dd.MM.yyyy"
                TIME_ONLY -> "HH:mm:ss"
                HuM -> "HH:mm"
                PLOT -> "yyyy-MM-dd HH:mm:ss"
                FILENAME -> "yyyy-MM-dd-HH-mm-ss"
                EXCHANGE -> "yyyy-MM-dd HH:mm:ss"
            }, Locale.getDefault()
        )

        if (format == EXCHANGE)
            formatter.timeZone = zone
        formatter.format(Date(millis))
    }
}