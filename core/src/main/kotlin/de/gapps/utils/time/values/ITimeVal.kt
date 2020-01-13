package de.gapps.utils.time.values

import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.base.IMillisecondHolder
import de.gapps.utils.time.time
import java.io.Serializable

interface ITimeVal<out T> : IMillisecondHolder, Serializable {

    val value: T

    operator fun component1(): ITimeEx = time
    operator fun component2(): T = value
}