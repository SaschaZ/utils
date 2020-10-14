@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.misc

import java.awt.Dimension
import java.awt.Point
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import kotlin.math.max
import kotlin.math.min


data class Vec2d(val x: Double, val y: Double) {

    constructor(x: Number, y: Number) : this(x.toDouble(), y.toDouble())

    fun toPoint() = Point(x.toInt(), y.toInt())

    operator fun unaryMinus() = this * -1
    operator fun plus(v: Vec2d) = Vec2d(x + v.x, y + v.y)
    operator fun plus(n: Number) = Vec2d(x + n.toDouble(), y + n.toDouble())
    operator fun minus(v: Vec2d) = Vec2d(x - v.x, y - v.y)
    operator fun minus(n: Number) = Vec2d(x - n.toDouble(), y - n.toDouble())
    operator fun times(v: Vec2d) = Vec2d(x * v.x, y * v.y)
    operator fun times(n: Number) = Vec2d(x * n.toDouble(), y * n.toDouble())
    operator fun div(v: Vec2d) = Vec2d(x / v.x, y / v.y)
    operator fun div(n: Number) = Vec2d(x / n.toDouble(), y / n.toDouble())

    operator fun compareTo(other: Vec2d) = (x.compareTo(other.x) + y.compareTo(other.y)) / 2

    val min: Double
        get() = min(x, y)
    val max: Double
        get() = max(x, y)
    val average: Double
        get() = (max + min) / 2

    fun inRange(v: Vec2d) = inRange(v.x, v.y)
    fun inRange(min: Number, max: Number) =
        Vec2d(
            max(min.toDouble(), min(max.toDouble(), x)),
            max(min.toDouble(), min(max.toDouble(), y))
        )

    fun pow(exp: Number) = Vec2d(x.pow(exp), y.pow(exp))

    fun toString(decimals: Int) = "(${x.format(decimals)}/${y.format(decimals)})"
}

operator fun Double.plus(v: Vec2d) = v + this
operator fun Double.minus(v: Vec2d) = Vec2d(this - v.x, this - v.y)
operator fun Double.times(v: Vec2d) = v * this
operator fun Double.div(v: Vec2d) = Vec2d(this / v.x, this / v.y)

operator fun Float.plus(v: Vec2d) = v + this
operator fun Float.minus(v: Vec2d) = Vec2d(this - v.x, this - v.y)
operator fun Float.times(v: Vec2d) = v * this
operator fun Float.div(v: Vec2d) = Vec2d(this / v.x, this / v.y)

operator fun Int.plus(v: Vec2d) = v + this
operator fun Int.minus(v: Vec2d) = Vec2d(this - v.x, this - v.y)
operator fun Int.times(v: Vec2d) = v * this
operator fun Int.div(v: Vec2d) = Vec2d(this / v.x, this / v.y)

operator fun Number.plus(v: Vec2d) = v + this
operator fun Number.minus(v: Vec2d) =
    Vec2d(this.toDouble() - v.x, this.toDouble() - v.y)

operator fun Number.times(v: Vec2d) = v * this
operator fun Number.div(v: Vec2d) =
    Vec2d(this.toDouble() / v.x, this.toDouble() / v.y)

fun Point2D.toVec2d() = Vec2d(x, y)
fun Dimension.toVec2d() = Vec2d(width, height)

val BufferedImage.size: Vec2d
    get() = Vec2d(width, height)