@file:Suppress("unused")

package dev.zieger.utils.time.base

import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.duration.DurationEx
import dev.zieger.utils.time.duration.IDurationEx

/**
 * plus
 */
operator fun ITimeEx.plus(other: Number): ITimeEx =
    TimeEx(nanos + other.bigI)

operator fun INanoTime.plus(other: INanoTime): INanoTime =
    NanoTime(nanos + other.nanos)

operator fun IDurationEx.plus(other: Number): IDurationEx =
    DurationEx(nanos + other.bigI)

operator fun ITimeEx.plus(other: ITimeEx): IDurationEx =
    DurationEx(nanos + other.nanos)

operator fun ITimeEx.plus(other: IDurationEx): ITimeEx =
    TimeEx(nanos + other.nanos)

operator fun IDurationEx.plus(other: IDurationEx): IDurationEx =
    DurationEx(nanos + other.nanos)

operator fun IDurationEx.plus(other: ITimeEx): ITimeEx =
    TimeEx(nanos + other.nanos)

operator fun Number.plus(other: IDurationEx): IDurationEx =
    DurationEx(bigI + other.nanos)

operator fun Number.plus(other: ITimeEx): ITimeEx =
    TimeEx(bigI + other.nanos)


/**
 * minus
 */
operator fun ITimeEx.minus(other: Number): ITimeEx =
    TimeEx(nanos - other.bigI)

operator fun INanoTime.minus(other: INanoTime): INanoTime =
    NanoTime(nanos - other.nanos)

operator fun IDurationEx.minus(other: Number): IDurationEx =
    DurationEx(nanos - other.bigI)

operator fun ITimeEx.minus(other: ITimeEx): IDurationEx =
    DurationEx(nanos - other.nanos)

operator fun ITimeEx.minus(other: IDurationEx): ITimeEx =
    TimeEx(nanos - other.nanos)

operator fun IDurationEx.minus(other: IDurationEx): IDurationEx =
    DurationEx(nanos - other.nanos)

operator fun IDurationEx.minus(other: ITimeEx): ITimeEx =
    TimeEx(nanos - other.nanos)

operator fun Number.minus(other: IDurationEx): IDurationEx =
    DurationEx(bigI - other.nanos)

operator fun Number.minus(other: ITimeEx): ITimeEx =
    TimeEx(bigI - other.nanos)


/**
 * times
 */
operator fun ITimeEx.times(other: Number): ITimeEx =
    TimeEx(nanos * other.bigI)

operator fun INanoTime.times(other: INanoTime): INanoTime =
    NanoTime(nanos * other.nanos)

operator fun IDurationEx.times(other: Number): IDurationEx =
    DurationEx(nanos * other.bigI)

operator fun ITimeEx.times(other: ITimeEx): IDurationEx =
    DurationEx(nanos * other.nanos)

operator fun ITimeEx.times(other: IDurationEx): ITimeEx =
    TimeEx(nanos * other.nanos)

operator fun IDurationEx.times(other: IDurationEx): IDurationEx =
    DurationEx(nanos * other.nanos)

operator fun IDurationEx.times(other: ITimeEx): ITimeEx =
    TimeEx(nanos * other.nanos)

operator fun Number.times(other: IDurationEx): IDurationEx =
    DurationEx(bigI * other.nanos)

operator fun Number.times(other: ITimeEx): ITimeEx =
    TimeEx(bigI * other.nanos)


/**
 * div
 */
operator fun ITimeEx.div(other: Number): ITimeEx =
    TimeEx(nanos / other.bigI)

operator fun INanoTime.div(other: INanoTime): INanoTime =
    NanoTime(nanos / other.nanos)

operator fun IDurationEx.div(other: Number): IDurationEx =
    DurationEx(nanos / other.bigI)

operator fun ITimeEx.div(other: ITimeEx): IDurationEx =
    DurationEx(nanos / other.nanos)

operator fun ITimeEx.div(other: IDurationEx): ITimeEx =
    TimeEx(nanos / other.nanos)

operator fun IDurationEx.div(other: IDurationEx): IDurationEx =
    DurationEx(nanos / other.nanos)

operator fun IDurationEx.div(other: ITimeEx): ITimeEx =
    TimeEx(nanos / other.nanos)

operator fun Number.div(other: IDurationEx): IDurationEx =
    DurationEx(bigI / other.nanos)

operator fun Number.div(other: ITimeEx): ITimeEx =
    TimeEx(bigI / other.nanos)


/**
 * rem
 */
operator fun ITimeEx.rem(other: Number): IDurationEx =
    DurationEx(nanos % other.bigI)

operator fun INanoTime.rem(other: INanoTime): INanoTime =
    NanoTime(nanos % other.nanos)

operator fun IDurationEx.rem(other: Number): IDurationEx =
    DurationEx(nanos % other.bigI)

operator fun ITimeEx.rem(other: ITimeEx): IDurationEx =
    DurationEx(nanos % other.nanos)

operator fun ITimeEx.rem(other: IDurationEx): IDurationEx =
    DurationEx(nanos % other.nanos)

operator fun IDurationEx.rem(other: IDurationEx): IDurationEx =
    DurationEx(nanos % other.nanos)

operator fun IDurationEx.rem(other: ITimeEx): IDurationEx =
    DurationEx(nanos % other.nanos)

operator fun Number.rem(other: IDurationEx): IDurationEx =
    DurationEx(bigI % other.nanos)

operator fun Number.rem(other: ITimeEx): ITimeEx =
    TimeEx(bigI % other.nanos)