package dev.zieger.utils.time

import dev.zieger.utils.misc.catch
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
            "yyyy-MM-dd HH:mm"
        )
    }

    protected fun String.stringToMillis(timeZone: TimeZone): Long {
        COMMON_DATA_FORMATS.forEach { format ->
            catch<Long?>(null, printStackTrace = false, logStackTrace = false) {
                val dateFormat = SimpleDateFormat(format, Locale.getDefault())
                dateFormat.timeZone = timeZone
                val result = dateFormat.parse(this)
                result.time
            }?.also { return it }
        }
        throw IllegalArgumentException("Can not parse Date from $this")
    }
}

fun String.parse(zone: TimeZone = TimeZone.getDefault()) = TimeEx(this, zone)