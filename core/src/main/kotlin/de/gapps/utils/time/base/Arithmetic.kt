package de.gapps.utils.time.base

import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.duration.DurationEx
import de.gapps.utils.time.duration.IDurationEx

operator fun ITimeEx.plus(other: Number): ITimeEx =
    TimeEx(millis + other.toLong())
operator fun IDurationEx.plus(other: Number): IDurationEx =
    DurationEx(millis + other.toLong())

operator fun ITimeEx.plus(other: ITimeEx): IDurationEx =
    DurationEx(millis + other.millis)
operator fun ITimeEx.plus(other: IDurationEx): ITimeEx =
    TimeEx(millis + other.millis)
operator fun IDurationEx.plus(other: IDurationEx): IDurationEx =
    DurationEx(millis + other.millis)
operator fun IDurationEx.plus(other: ITimeEx): ITimeEx =
    TimeEx(millis + other.millis)

operator fun Number.plus(other: IDurationEx): IDurationEx =
    DurationEx(toLong() + other.millis)
operator fun Number.plus(other: ITimeEx): ITimeEx =
    TimeEx(toLong() + other.millis)



operator fun ITimeEx.minus(other: Number): ITimeEx =
    TimeEx(millis - other.toLong())
operator fun IDurationEx.minus(other: Number): IDurationEx =
    DurationEx(millis - other.toLong())

operator fun ITimeEx.minus(other: ITimeEx): IDurationEx =
    DurationEx(millis - other.millis)
operator fun ITimeEx.minus(other: IDurationEx): ITimeEx =
    TimeEx(millis - other.millis)
operator fun IDurationEx.minus(other: IDurationEx): IDurationEx =
    DurationEx(millis - other.millis)
operator fun IDurationEx.minus(other: ITimeEx): ITimeEx =
    TimeEx(millis - other.millis)

operator fun Number.minus(other: IDurationEx): IDurationEx =
    DurationEx(toLong() - other.millis)
operator fun Number.minus(other: ITimeEx): ITimeEx =
    TimeEx(toLong() - other.millis)



operator fun ITimeEx.times(other: Number): ITimeEx =
    TimeEx(millis * other.toLong())
operator fun IDurationEx.times(other: Number): IDurationEx =
    DurationEx(millis * other.toLong())

operator fun ITimeEx.times(other: ITimeEx): IDurationEx =
    DurationEx(millis * other.millis)
operator fun ITimeEx.times(other: IDurationEx): ITimeEx =
    TimeEx(millis * other.millis)
operator fun IDurationEx.times(other: IDurationEx): IDurationEx =
    DurationEx(millis * other.millis)
operator fun IDurationEx.times(other: ITimeEx): ITimeEx =
    TimeEx(millis * other.millis)

operator fun Number.times(other: IDurationEx): IDurationEx =
    DurationEx(toDouble() * other.millis)
operator fun Number.times(other: ITimeEx): ITimeEx =
    TimeEx(toDouble() * other.millis)



operator fun ITimeEx.div(other: Number): ITimeEx =
    TimeEx(millis / other.toLong())
operator fun IDurationEx.div(other: Number): IDurationEx =
    DurationEx(millis / other.toLong())

operator fun ITimeEx.div(other: ITimeEx): IDurationEx =
    DurationEx(millis / other.millis)
operator fun ITimeEx.div(other: IDurationEx): ITimeEx =
    TimeEx(millis / other.millis)
operator fun IDurationEx.div(other: IDurationEx): IDurationEx =
    DurationEx(millis / other.millis)
operator fun IDurationEx.div(other: ITimeEx): ITimeEx =
    TimeEx(millis / other.millis)

operator fun Number.div(other: IDurationEx): IDurationEx =
    DurationEx(toDouble() / other.millis)
operator fun Number.div(other: ITimeEx): ITimeEx =
    TimeEx(toDouble() / other.millis)



operator fun ITimeEx.rem(other: Number): IDurationEx =
    DurationEx(millis % other.toLong())
operator fun IDurationEx.rem(other: Number): IDurationEx =
    DurationEx(millis % other.toLong())

operator fun ITimeEx.rem(other: ITimeEx): IDurationEx =
    DurationEx(millis % other.millis)
operator fun ITimeEx.rem(other: IDurationEx): IDurationEx =
    DurationEx(millis % other.millis)
operator fun IDurationEx.rem(other: IDurationEx): IDurationEx =
    DurationEx(millis % other.millis)
operator fun IDurationEx.rem(other: ITimeEx): IDurationEx =
    DurationEx(millis % other.millis)

operator fun Number.rem(other: IDurationEx): IDurationEx =
    DurationEx(toLong() % other.millis)
operator fun Number.rem(other: ITimeEx): ITimeEx =
    TimeEx(toLong() % other.millis)