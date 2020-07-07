package dev.zieger.utils.time

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import dev.zieger.utils.json.JsonAdapter
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.time.duration.DurationEx
import dev.zieger.utils.time.duration.IDurationEx
import java.util.*

class TimeExJsonAdapter : JsonAdapter<ITimeEx>() {
    @ToJson
    override fun toJson(value: ITimeEx): String =
        "${value.millis}|${value.zone.id}"

    @FromJson
    override fun fromJson(json: String): ITimeEx =
        json.split("|").nullWhen { it.size < 2 }?.let { TimeEx(it[0].toLong(), TimeZone.getTimeZone(it[1])) }
            ?: json.parse(GMT)
}

@Suppress("HasPlatformType")
val GMT
    get() = TimeZone.getTimeZone("GMT-0:00")

class DurationExJsonAdapter : JsonAdapter<IDurationEx>() {
    @ToJson
    override fun toJson(value: IDurationEx): String = "${value.millis}"

    @FromJson
    override fun fromJson(json: String): IDurationEx = DurationEx(json.toLong())
}

class ClosedTimeRangeJsonAdapter : JsonAdapter<ClosedRange<ITimeEx>>() {

    @ToJson
    override fun toJson(value: ClosedRange<ITimeEx>): String =
        "${value.start.run { "$millis|${zone.id}" }}..${value.endInclusive.run { "$millis|${zone.id}" }}"

    @FromJson
    override fun fromJson(json: String): ClosedRange<ITimeEx> = json.split("..").let {
        it[0].split("|").let { l -> TimeEx(l[0].toLong(), TimeZone.getTimeZone(l[1])) }..
                it[1].split("|").let { l -> TimeEx(l[0].toLong(), TimeZone.getTimeZone(l[1])) }
    }
}