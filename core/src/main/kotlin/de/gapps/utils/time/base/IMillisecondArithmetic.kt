package de.gapps.utils.time.base

import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.duration.DurationEx
import de.gapps.utils.time.duration.IDurationEx
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

interface IMillisecondArithmetic<out T : IMillisecondHolder, C : T> : IMillisecondHolderComparator {

    val clazz: KClass<@UnsafeVariance C>

    @Suppress("UNCHECKED_CAST")
    fun newInstance(millis: Number): T = clazz.primaryConstructor!!.call(millis.toLong())


    operator fun plus(other: IMillisecondHolder): T =
        newInstance(millis + other.millis)

    operator fun plus(other: Number): T =
        newInstance(millis + other.toLong())


    operator fun minus(other: IMillisecondHolder): T =
        newInstance(millis - other.millis)

    operator fun minus(other: Number): T =
        newInstance(millis - other.toLong())


    operator fun times(other: IMillisecondHolder): T =
        newInstance(millis.toDouble() * other.millis.toDouble())

    operator fun times(other: Number): T =
        newInstance(millis.toDouble() * other.toDouble())


    operator fun div(other: IMillisecondHolder): T =
        newInstance(millis.toDouble() / other.millis.toDouble())

    operator fun div(other: Number): T =
        newInstance(millis.toDouble() / other.toDouble())


    operator fun rem(other: IMillisecondHolder): T =
        newInstance(millis % other.millis)

    operator fun rem(other: Number): T =
        newInstance(millis % other.toLong())
}

operator fun Number.plus(other: IDurationEx): IDurationEx =
    DurationEx(toLong() + other.millis)

operator fun Number.plus(other: ITimeEx): ITimeEx =
    TimeEx(toLong() + other.millis)

operator fun Number.minus(other: IDurationEx): IDurationEx =
    DurationEx(toLong() - other.millis)

operator fun Number.minus(other: ITimeEx): ITimeEx =
    TimeEx(toLong() - other.millis)

operator fun Number.times(other: IDurationEx): IDurationEx =
    DurationEx(toDouble() * other.millis)

operator fun Number.times(other: ITimeEx): ITimeEx =
    TimeEx(toDouble() * other.millis)

operator fun Number.div(other: IDurationEx): IDurationEx =
    DurationEx(toDouble() / other.millis)

operator fun Number.div(other: ITimeEx): ITimeEx =
    TimeEx(toDouble() / other.millis)

operator fun Number.rem(other: IDurationEx): IDurationEx =
    DurationEx(toLong() % other.millis)

operator fun Number.rem(other: ITimeEx): ITimeEx =
    TimeEx(toLong() % other.millis)