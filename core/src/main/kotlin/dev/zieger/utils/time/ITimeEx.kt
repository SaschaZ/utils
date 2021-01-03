package dev.zieger.utils.time

import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.string.StringConverter
import dev.zieger.utils.time.zone.ITimeZoneHolder
import java.util.*

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

    val daysInMonth: Int
        get() = when (month + 1) {
            2 -> 28
            1, 3, 5, 7, 8, 10, 12 -> 31
            else -> 30
        }
}

