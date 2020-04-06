package de.gapps.utils.time

import de.gapps.utils.time.base.IMillisecondHolderComparator
import de.gapps.utils.time.zone.ITimeZoneHolder
import java.util.*

interface ITimeEx : IMillisecondHolderComparator, ITimeZoneHolder, StringConverter {

    val date: Date
        get() = Date(millis + zone.rawOffset + zone.dstSavings)

    val calendar: Calendar
        get() = Calendar.getInstance(zone).apply { timeInMillis = millis }

    /**
     * @see [Calendar.get]
     */
    fun get(field: Int) = calendar.get(field)

    val month: Int
        get() = get(Calendar.MONTH)
    val dayOfMonth: Int
        get() = get(Calendar.DAY_OF_MONTH)
    val dayOfWeek: Int
        get() = get(Calendar.DAY_OF_WEEK)
}

