package dev.zieger.utils.time

import dev.zieger.utils.json.JsonAdapter
import dev.zieger.utils.time.base.TimeUnit
import dev.zieger.utils.time.duration.DurationEx
import dev.zieger.utils.time.duration.IDurationEx

class TimeExJsonAdapter : JsonAdapter<ITimeEx>() {
    override fun toJson(value: ITimeEx): String = "$value"
    override fun fromJson(json: String): ITimeEx = TimeEx(json)
}

class DurationExJsonAdapter : JsonAdapter<IDurationEx>() {
    override fun toJson(value: IDurationEx): String = "${value.millis}"
    override fun fromJson(json: String): IDurationEx = DurationEx(json.toLong(), TimeUnit.MILLI)
}