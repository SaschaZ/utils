@file:Suppress("USELESS_CAST", "unused")

package dev.zieger.utils.misc

import dev.zieger.utils.time.duration.milliseconds
import kotlin.math.pow

interface INumber {
    /**
     * Returns the value of this number as a [Double], which may involve rounding.
     */
    fun toDouble(): Double

    /**
     * Returns the value of this number as a [Float], which may involve rounding.
     */
    fun toFloat(): Float

    /**
     * Returns the value of this number as a [Long], which may involve rounding or truncation.
     */
    fun toLong(): Long

    /**
     * Returns the value of this number as an [Int], which may involve rounding or truncation.
     */
    fun toInt(): Int

    /**
     * Returns the [Char] with the numeric value equal to this number, truncated to 16 bits if appropriate.
     */
    fun toChar(): Char

    /**
     * Returns the value of this number as a [Short], which may involve rounding or truncation.
     */
    fun toShort(): Short

    /**
     * Returns the value of this number as a [Byte], which may involve rounding or truncation.
     */
    fun toByte(): Byte
}

open class NumberEx(internal val internal: Number) : Number(), Comparable<NumberEx> {

    constructor(value: Double) : this(value as Number)
    constructor(value: Float) : this(value as Number)
    constructor(value: Int) : this(value as Number)
    constructor(value: Long) : this(value as Number)
    constructor(value: Short) : this(value as Number)
    constructor(value: Char) : this(value.toShort() as Number)
    constructor(value: Byte) : this(value as Number)

    constructor(value: NumberEx) : this(value.internal)


    override fun compareTo(other: NumberEx): Int = internal.milliseconds.compareTo(other.milliseconds)

    override fun equals(other: Any?) = (other as? NumberEx)?.internal == internal || (other as? Number) == internal

    override fun hashCode() = internal.hashCode()

    override fun toByte() = internal.toByte()

    override fun toChar() = internal.toChar()

    override fun toDouble() = internal.toDouble()

    override fun toFloat() = internal.toFloat()

    override fun toInt() = internal.toInt()

    override fun toLong() = internal.toLong()

    override fun toShort() = internal.toShort()
}

operator fun <TL : Number, TR : Number> TL.plus(o: TR): NumberEx {
    val left = (this as? NumberEx)?.internal ?: this
    val right = (o as? NumberEx)?.internal ?: o
    return left.run {
        when {
            this is Double || right is Double -> toDouble().plus(right.toDouble()).ex
            this is Float || right is Float -> toFloat().plus(right.toFloat()).ex
            this is Long || right is Long -> toLong().plus(right.toLong()).ex
            this is Int || right is Int -> toInt().plus(right.toInt()).ex
            this is Short || right is Short -> toShort().plus(right.toShort()).ex
            this is Byte || right is Byte -> toByte().plus(right.toByte()).ex
            else -> throw IllegalArgumentException("unsupported Number type $this")
        }
    }
}

operator fun <TL : Number, TR : Number> TL.minus(o: TR): NumberEx {
    val left = (this as? NumberEx)?.internal ?: this
    val right = (o as? NumberEx)?.internal ?: o
    return left.run {
        when {
            this is Double || right is Double -> toDouble().minus(right.toDouble()).ex
            this is Float || right is Float -> toFloat().minus(right.toFloat()).ex
            this is Long || right is Long -> toLong().minus(right.toLong()).ex
            this is Int || right is Int -> toInt().minus(right.toInt()).ex
            this is Short || right is Short -> toShort().minus(right.toShort()).ex
            this is Byte || right is Byte -> toByte().minus(right.toByte()).ex
            else -> throw IllegalArgumentException("unsupported Number type $this")
        }
    }
}

operator fun <TL : Number, TR : Number> TL.times(o: TR): NumberEx {
    val left = (this as? NumberEx)?.internal ?: this
    val right = (o as? NumberEx)?.internal ?: o
    return left.run {
        when {
            this is Double || right is Double -> toDouble().times(right.toDouble()).ex
            this is Float || right is Float -> toFloat().times(right.toFloat()).ex
            this is Long || right is Long -> toLong().times(right.toLong()).ex
            this is Int || right is Int -> toInt().times(right.toInt()).ex
            this is Short || right is Short -> toShort().times(right.toShort()).ex
            this is Byte || right is Byte -> toByte().times(right.toByte()).ex
            else -> throw IllegalArgumentException("unsupported Number type $this")
        }
    }
}

operator fun <TL : Number, TR : Number> TL.div(o: TR): NumberEx {
    val left = (this as? NumberEx)?.internal ?: this
    val right = (o as? NumberEx)?.internal ?: o
    return left.run {
        when {
            this is Double || right is Double -> toDouble().div(right.toDouble()).ex
            this is Float || right is Float -> toFloat().div(right.toFloat()).ex
            this is Long || right is Long -> toLong().div(right.toLong()).ex
            this is Int || right is Int -> toInt().div(right.toInt()).ex
            this is Short || right is Short -> toShort().div(right.toShort()).ex
            this is Byte || right is Byte -> toByte().div(right.toByte()).ex
            else -> throw IllegalArgumentException("unsupported Number type $this")
        }
    }
}

operator fun <TL : Number, TR : Number> TL.rem(o: TR): NumberEx {
    val left = (this as? NumberEx)?.internal ?: this
    val right = (o as? NumberEx)?.internal ?: o
    return left.run {
        when {
            this is Double || right is Double -> toDouble().rem(right.toDouble()).ex
            this is Float || right is Float -> toFloat().rem(right.toFloat()).ex
            this is Long || right is Long -> toLong().rem(right.toLong()).ex
            this is Int || right is Int -> toInt().rem(right.toInt()).ex
            this is Short || right is Short -> toShort().rem(right.toShort()).ex
            this is Byte || right is Byte -> toByte().rem(right.toByte()).ex
            else -> throw IllegalArgumentException("unsupported Number type $this")
        }
    }
}


infix fun <T : Number, P : Number> T.pow(exp: P) = toDouble().pow(exp.toDouble())

val <T : Number> T.ex
    get() = NumberEx(this)

val <T : Number> Iterable<T>.ex
    get() = map { NumberEx(it) }


fun <T> Iterable<T>.derivation(grade: Int = 1, selector: (T) -> Number) = map { selector(it) }.derivation(grade)

fun <T : Number> Iterable<T>.derivation(grade: Int = 1): List<NumberEx> {
    var result: List<NumberEx>? = null
    repeat(grade) { result = (result ?: this).toList().mapPrevNotNull { cur, prev -> cur - prev } }
    return result!!
}

fun <T : NumberEx> Iterable<T>.median() = if (iterator().hasNext()) (max()!! - min()!!) / 2.0 else 0.0.ex