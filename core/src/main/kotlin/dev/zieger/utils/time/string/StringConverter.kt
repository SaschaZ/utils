package dev.zieger.utils.time.string

import dev.zieger.utils.time.base.IDurationHolder
import dev.zieger.utils.time.string.DateFormat.COMPLETE
import dev.zieger.utils.time.zone.ITimeZoneHolder
import java.text.SimpleDateFormat
import java.util.*


enum class DateFormat(val pattern: String) {

    COMPLETE("dd.MM.yyyy-HH:mm:ss"),
    DATE_ONLY("dd.MM.yyyy"),
    TIME_ONLY("HH:mm:ss"),
    HuM("HH:mm"),
    PLOT("yyyy-MM-dd HH:mm:ss"),
    FILENAME("yyyy-MM-dd-HH-mm-ss"),
    FILENAME_DATE("yyyy-MM-dd"),
    FILENAME_TIME("HH-mm-ss"),
    EXCHANGE("yyyy-MM-dd HH:mm:ss")
}

interface StringConverter : IDurationHolder, ITimeZoneHolder {

    fun formatTime(format: DateFormat = COMPLETE, altZone: TimeZone? = null): String =
        StringConverterDelegate.formatTime(
            format,
            millis,
            altZone ?: zone
        )
}

object StringConverterDelegate {

    var formatTime: (DateFormat, Long, TimeZone) -> String = { format, millis, zone ->
        val formatter = SimpleDateFormat(format.pattern, Locale.getDefault())
        formatter.timeZone = zone
        formatter.format(Date(millis))
    }
}