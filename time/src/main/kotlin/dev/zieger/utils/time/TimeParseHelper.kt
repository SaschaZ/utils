package dev.zieger.utils.time
import java.text.SimpleDateFormat
import java.util.*

open class TimeParseHelper {

    companion object {

        private val COMMON_DATA_FORMATS = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "dd.MM.yyyy-HH:mm:ss",
            "dd.MM.yyyy-HH:mm",
            "yyyy.MM.dd-HH:mm:ss",
            "yyyy.MM.dd-HH:mm",
            "dd.MM.yyyy HH:mm:ss",
            "dd.MM.yyyy HH:mm",
            "yyyy.MM.dd HH:mm:ss",
            "yyyy.MM.dd HH:mm",
            "dd-MM-yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "dd.MM.yyyy",
            "dd.MM.yy"
        )
    }

    protected fun String.stringToMillis(timeZone: TimeZone? = null): Long {
        COMMON_DATA_FORMATS.forEach { format ->
            try {
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                timeZone?.let { dateFormat.timeZone = it }
                val result = dateFormat.parse(this)
                return result.time
            } catch (t: Throwable) {
            }
        }
        throw IllegalArgumentException("Can not parse Date from $this")
    }
}

fun String.parse(zone: TimeZone? = null) = TimeStamp(this, zone)