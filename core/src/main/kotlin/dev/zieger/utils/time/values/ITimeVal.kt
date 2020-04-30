package dev.zieger.utils.time.values

import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.duration.IDurationHolder
import dev.zieger.utils.time.time
import java.io.Serializable

interface ITimeVal<out T> : IDurationHolder, Serializable {

    val value: T

    operator fun component1(): ITimeEx = time
    operator fun component2(): T = value
}