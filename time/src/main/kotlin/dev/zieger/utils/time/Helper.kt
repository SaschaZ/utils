package dev.zieger.utils.time

import java.util.*


private val ITimeStamp.calendar: Calendar
    get() = (zone?.let { Calendar.getInstance(it) } ?: Calendar.getInstance()).apply {
        timeInMillis = millis.toLong()
    }

/**
 * @see [Calendar.get]
 */
private fun ITimeStamp.getCalendarField(field: Int) = calendar.get(field)

val ITimeStamp.year: Int get() = getCalendarField(Calendar.YEAR)
val ITimeStamp.monthOfYear: Int get() = getCalendarField(Calendar.MONTH)
val ITimeStamp.weekOfYear: Int get() = getCalendarField(Calendar.WEEK_OF_YEAR)
val ITimeStamp.dayOfYear: Int get() = getCalendarField(Calendar.DAY_OF_YEAR)
val ITimeStamp.dayOfMonth: Int get() = getCalendarField(Calendar.DAY_OF_MONTH)
val ITimeStamp.dayOfWeek: Int get() = getCalendarField(Calendar.DAY_OF_WEEK)
val ITimeStamp.hourOfDay: Int get() = getCalendarField(Calendar.HOUR_OF_DAY)
val ITimeStamp.minuteOfHour: Int get() = getCalendarField(Calendar.MINUTE)
val ITimeStamp.secondOfMinute: Int get() = getCalendarField(Calendar.SECOND)
val ITimeStamp.milliOfSecond: Int get() = getCalendarField(Calendar.MILLISECOND)

val ITimeStamp.daysInMonth: Int get() = (monthOfYear + 1).daysInMonth(year)

val ITimeStamp.startOfYear: ITimeStamp get() = "1.1.$year".parse(zone)
val ITimeStamp.startOfMonth: ITimeStamp get() = "1.${monthOfYear + 1}.$year".parse(zone)
val ITimeStamp.startOfDay: ITimeStamp get() = "$dayOfMonth.${monthOfYear + 1}.$year".parse(zone)
val ITimeStamp.startOfHour: ITimeStamp get() = "$dayOfMonth.${monthOfYear + 1}.$year-$hourOfDay:00:00".parse(zone)
val ITimeStamp.startOfMinute: ITimeStamp
    get() = "$dayOfMonth.${monthOfYear + 1}.$year-$hourOfDay:$minuteOfHour:00".parse(
        zone
    )
val ITimeStamp.startOfSecond: ITimeStamp
    get() = "$dayOfMonth.${monthOfYear + 1}.$year-$hourOfDay:$minuteOfHour:$secondOfMinute".parse(
        zone
    )

val ITimeStamp.accordingDaySpan: ITimeSpan get() = this - startOfDay

val ITimeStamp.daysOfWeek: ITimeSpan get() = dayOfWeek.days
val ITimeStamp.hoursOfDay: ITimeSpan get() = hours.hours
val ITimeStamp.minutesOfHour: ITimeSpan get() = minuteOfHour.minutes
val ITimeStamp.secondsOfMinute: ITimeSpan get() = secondOfMinute.seconds
val ITimeStamp.millisOfSecond: ITimeSpan get() = milliOfSecond.millis