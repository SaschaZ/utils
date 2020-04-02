package dev.zieger.utils.time

import dev.zieger.utils.log.Log
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*

open class TimeParseHelper {

    protected fun String.stringToMillis(): Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
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