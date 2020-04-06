package de.gapps.utils.time

import java.text.SimpleDateFormat
import java.util.*

open class TimeParseHelper {

    protected fun String.stringToMillis(timeZone: TimeZone): Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
            dateFormat.timeZone = timeZone
            val result = dateFormat.parse(this)
            result.time
        } catch (t: Throwable) {
            throw IllegalArgumentException("Can not parse Date from $this")
        }
    }
}

fun String.parse() = TimeEx(this)