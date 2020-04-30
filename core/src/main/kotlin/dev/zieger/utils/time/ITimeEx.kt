package dev.zieger.utils.time

import dev.zieger.utils.time.duration.IDurationEx
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
    fun get(field: Int) = calendar.get(field)

    val year: Int
        get() = get(Calendar.YEAR)
    val month: Int
        get() = get(Calendar.MONTH)
    val dayOfMonth: Int
        get() = get(Calendar.DAY_OF_MONTH)
    val dayOfWeek: Int
        get() = get(Calendar.DAY_OF_WEEK)
    val hourOfDay: Int
        get() = get(Calendar.HOUR_OF_DAY)
}

