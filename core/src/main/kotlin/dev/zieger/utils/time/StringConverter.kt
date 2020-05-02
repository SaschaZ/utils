package dev.zieger.utils.time

import dev.zieger.utils.time.DateFormat.*
import dev.zieger.utils.time.base.INanoTime
import dev.zieger.utils.time.zone.ITimeZoneHolder
import java.text.SimpleDateFormat
import java.util.*


enum class DateFormat {

    COMPLETE,
    DATE_ONLY,
    TIME_ONLY,
    HuM,
    PLOT,
    FILENAME,
    FILENAME_DATE,
    FILENAME_TIME,
    EXCHANGE
}

interface StringConverter : INanoTime, ITimeZoneHolder {

    fun formatTime(format: DateFormat = COMPLETE): String =
        StringConverterDelegate.formatTime(format, millis.toLong(), zone)
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
                FILENAME_DATE -> "yyy-MM-dd"
                FILENAME_TIME -> "HH-mm-ss"
                EXCHANGE -> "yyyy-MM-dd HH:mm:ss"
            }, Locale.getDefault()
        )

        if (format == EXCHANGE)
            formatter.timeZone = zone
        formatter.format(Date(millis))
    }
}