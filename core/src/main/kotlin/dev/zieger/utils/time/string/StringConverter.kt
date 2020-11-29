@file:Suppress("ClassName", "unused")

package dev.zieger.utils.time.string

import dev.zieger.utils.time.base.IDurationHolder
import dev.zieger.utils.time.string.DateFormat.COMPLETE
import dev.zieger.utils.time.zone.ITimeZoneHolder
import java.text.SimpleDateFormat
import java.util.*


sealed class DateFormat(val pattern: String) {

    object COMPLETE : DateFormat("dd.MM.yyyy-HH:mm:ss")
    object DATE_ONLY : DateFormat("dd.MM.yyyy")
    object TIME_ONLY : DateFormat("HH:mm:ss-SSS")
    object HaM : DateFormat("HH:mm")
    object PLOT : DateFormat("yyyy-MM-dd HH:mm:ss")
    object FILENAME : DateFormat("yyyy-MM-dd-HH-mm-ss")
    object FILENAME_DATE : DateFormat("yyyy-MM-dd")
    object FILENAME_TIME : DateFormat("HH-mm-ss")
    object EXCHANGE : DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    open class CUSTOM(pattern: String) : DateFormat(pattern)
}

interface StringConverter : IDurationHolder, ITimeZoneHolder {

    fun formatTime(format: DateFormat = COMPLETE, altZone: TimeZone? = null): String =
        StringConverterDelegate.formatTime(format, millis, altZone ?: zone)
}

object StringConverterDelegate {

    var formatTime: (DateFormat, Long, TimeZone) -> String = { format, millis, zone ->
        val formatter = SimpleDateFormat(format.pattern, Locale.getDefault())
        formatter.timeZone = zone
        formatter.format(Date(millis))
    }
}