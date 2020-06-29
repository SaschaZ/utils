package dev.zieger.utils.time

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import dev.zieger.utils.json.JsonAdapter
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.duration.DurationEx
import dev.zieger.utils.time.duration.IDurationEx
import java.util.*

class TimeExJsonAdapter : JsonAdapter<ITimeEx>() {
    @ToJson
    override fun toJson(value: ITimeEx): String =
        value.formatTime(DateFormat.EXCHANGE, GMT)

    @FromJson
    override fun fromJson(json: String): ITimeEx = json.parse(GMT)
}

@Suppress("HasPlatformType")
val GMT
    get() = TimeZone.getTimeZone("GMT-0:00")

class DurationExJsonAdapter : JsonAdapter<IDurationEx>() {
    @ToJson
    override fun toJson(value: IDurationEx): String = "${value.millis}"

    @FromJson
    override fun fromJson(json: String): IDurationEx = DurationEx(json.toLong(), TimeUnit.MILLI)
}