package dev.zieger.utils.time.values

import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.base.INanoTime
import dev.zieger.utils.time.time
import java.io.Serializable

interface ITimeVal<out T> : INanoTime, Serializable {

    val value: T

    operator fun component1(): ITimeEx = time
    operator fun component2(): T = value
}