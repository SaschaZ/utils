package de.gapps.utils.time

import de.gapps.utils.log.Log
import java.text.SimpleDateFormat
import java.time.ZonedDateTime

open class TimeParseHelper {

    protected fun String.stringToMillis(): Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            val result = dateFormat.parse(this)
            result.time
        } catch (t: Throwable) {
            try {
                val result = ZonedDateTime.parse(this).toInstant().toEpochMilli()
                Log.v("stringToNanos! catched -> $this -> $result ($t)")
                result
            } catch (t: Throwable) {
                throw IllegalArgumentException("Can not parse Date from $this")
            }
        }
    }
}

fun String.parse() = TimeEx(this)