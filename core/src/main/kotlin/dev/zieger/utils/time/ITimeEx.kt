package dev.zieger.utils.time

import dev.zieger.utils.misc.min
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.string.StringConverter
import dev.zieger.utils.time.string.parse
import dev.zieger.utils.time.zone.ITimeZoneHolder
import java.util.*
import kotlin.math.absoluteValue

interface ITimeEx : IDurationEx, ITimeZoneHolder, StringConverter {

    val date: Date
        get() = Date(millis + zone.rawOffset + zone.dstSavings)

    val calendar: Calendar
        get() = Calendar.getInstance(zone).apply { timeInMillis = millis }

    /**
     * @see [Calendar.get]
     */
    fun getCalendarField(field: Int) = calendar.get(field)

    val year: Int
        get() = getCalendarField(Calendar.YEAR)
    val month: Int
        get() = getCalendarField(Calendar.MONTH)
    val weekOfYear: Int
        get() = getCalendarField(Calendar.WEEK_OF_YEAR)
    val dayOfYear: Int
        get() = getCalendarField(Calendar.DAY_OF_YEAR)
    val dayOfMonth: Int
        get() = getCalendarField(Calendar.DAY_OF_MONTH)
    val dayOfWeek: Int
        get() = getCalendarField(Calendar.DAY_OF_WEEK)
    val hourOfDay: Int
        get() = getCalendarField(Calendar.HOUR_OF_DAY)
    val minuteOfHour: Int
        get() = getCalendarField(Calendar.MINUTE)
    val secondOfMinute: Int
        get() = getCalendarField(Calendar.MINUTE)
    val milliOfSecond: Int
        get() = getCalendarField(Calendar.MILLISECOND)

    val daysInMonth: Int get() = (month + 1).daysInMonth

    fun addCalendarDays(delta: Int): ITimeEx {
        var day = dayOfMonth
        var month = month + 1
        var year = years

        repeat(delta.absoluteValue) {
            day += if (delta > 0) 1 else -1
            when {
                day > month.daysInMonth -> {
                    day -= month.daysInMonth
                    month++
                }
                day < 1 -> {
                    month--
                    day += month.daysInMonth
                }
            }
            when {
                month > 12 -> {
                    year++
                    month = 1
                }
                month < 1 -> {
                    year--
                    month = 12
                }
            }
        }
        return "$day.$month.$year'T'$hourOfDay:$minuteOfHour:$secondOfMinute.${milliOfSecond}X".parse(zone)
    }

    fun addCalendarMonths(delta: Int): ITimeEx {
        var month = month + 1
        var year = years

        repeat(delta.absoluteValue) {
            month += if (delta > 0) 1 else -1
            when {
                month > 12 -> {
                    month -= 12
                    year++
                }
                month < 1 -> {
                    year--
                    month += 12
                }
            }
        }
        val day = min(dayOfMonth, month.daysInMonth)
        return "$day.$month.$year'T'$hourOfDay:$minuteOfHour:$secondOfMinute.${milliOfSecond}X".parse(zone)
    }
}

internal val Int.daysInMonth: Int
    get() = when (this) {
        2 -> 28
        1, 3, 5, 7, 8, 10, 12 -> 31
        else -> 30
    }

