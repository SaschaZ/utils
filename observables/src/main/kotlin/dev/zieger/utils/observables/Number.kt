package dev.zieger.utils.observables


operator fun kotlin.Number.unaryPlus(): kotlin.Number = Number(this)

class Number(var value: kotlin.Number) : kotlin.Number(), Comparable<kotlin.Number>, Comparator<kotlin.Number> {

    override fun toByte(): Byte = value.toByte()
    override fun toChar(): Char = value.toChar()
    override fun toDouble(): Double = value.toDouble()
    override fun toFloat(): Float = value.toFloat()
    override fun toInt(): Int = value.toInt()
    override fun toLong(): Long = value.toLong()
    override fun toShort(): Short = value.toShort()

    operator fun unaryMinus(): Number {
        return this * -1
    }

    operator fun plus(other: kotlin.Number): Number = when {
        value is Double || other is Double -> value.toDouble() + other.toDouble()
        value is Float || other is Float -> value.toFloat() + other.toFloat()
        value is Long || other is Long -> value.toLong() + other.toLong()
        value is Int || other is Int -> value.toInt() + other.toInt()
        value is Short || other is Short -> value.toShort() + other.toShort()
        value is Byte || other is Byte -> value.toByte() + other.toByte()
        else -> throw IllegalArgumentException("Unknown type ${value::class}")
    }.let { Number(it) }

    operator fun plusAssign(other: kotlin.Number) {
        value = this + other
    }

    operator fun minus(other: kotlin.Number): Number = when {
        value is Double || other is Double -> value.toDouble() - other.toDouble()
        value is Float || other is Float -> value.toFloat() - other.toFloat()
        value is Long || other is Long -> value.toLong() - other.toLong()
        value is Int || other is Int -> value.toInt() - other.toInt()
        value is Short || other is Short -> value.toShort() - other.toShort()
        value is Byte || other is Byte -> value.toByte() - other.toByte()
        else -> throw IllegalArgumentException("Unknown type ${value::class}")
    }.let { Number(it) }

    operator fun minusAssign(other: kotlin.Number) {
        value = this - other
    }

    operator fun times(other: kotlin.Number): Number = when {
        value is Double || other is Double -> value.toDouble() * other.toDouble()
        value is Float || other is Float -> value.toFloat() * other.toFloat()
        value is Long || other is Long -> value.toLong() * other.toLong()
        value is Int || other is Int -> value.toInt() * other.toInt()
        value is Short || other is Short -> value.toShort() * other.toShort()
        value is Byte || other is Byte -> value.toByte() * other.toByte()
        else -> throw IllegalArgumentException("Unknown type ${value::class}")
    }.let { Number(it) }

    operator fun timesAssign(other: kotlin.Number) {
        value = this * other
    }

    operator fun div(other: kotlin.Number): Number = when {
        value is Double || other is Double -> value.toDouble() / other.toDouble()
        value is Float || other is Float -> value.toFloat() / other.toFloat()
        value is Long || other is Long -> value.toLong() / other.toLong()
        value is Int || other is Int -> value.toInt() / other.toInt()
        value is Short || other is Short -> value.toShort() / other.toShort()
        value is Byte || other is Byte -> value.toByte() / other.toByte()
        else -> throw IllegalArgumentException("Unknown type ${value::class}")
    }.let { Number(it) }

    operator fun divAssign(other: kotlin.Number) {
        value = this / other
    }

    operator fun rem(other: kotlin.Number): Number = when {
        value is Double || other is Double -> value.toDouble() % other.toDouble()
        value is Float || other is Float -> value.toFloat() % other.toFloat()
        value is Long || other is Long -> value.toLong() % other.toLong()
        value is Int || other is Int -> value.toInt() % other.toInt()
        value is Short || other is Short -> value.toShort() % other.toShort()
        value is Byte || other is Byte -> value.toByte() % other.toByte()
        else -> throw IllegalArgumentException("Unknown type ${value::class}")
    }.let { Number(it) }

    operator fun remAssign(other: kotlin.Number) {
        value = this % other
    }

    override fun compareTo(other: kotlin.Number): Int = when {
        value is Double || other is Double -> when {
            value.toDouble() < other.toDouble() -> -1
            value.toDouble() == other.toDouble() -> 0
            value.toDouble() > other.toDouble() -> 1
            else -> throw IllegalStateException("Unsupported operation")
        }
        value is Float || other is Float -> when {
            value.toFloat() < other.toFloat() -> -1
            value.toFloat() == other.toFloat() -> 0
            value.toFloat() > other.toFloat() -> 1
            else -> throw IllegalStateException("Unsupported operation")
        }
        value is Long || other is Long -> when {
            value.toLong() < other.toLong() -> -1
            value.toLong() == other.toLong() -> 0
            value.toLong() > other.toLong() -> 1
            else -> throw IllegalStateException("Unsupported operation")
        }
        value is Int || other is Int -> when {
            value.toInt() < other.toInt() -> -1
            value.toInt() == other.toInt() -> 0
            value.toInt() > other.toInt() -> 1
            else -> throw IllegalStateException("Unsupported operation")
        }
        value is Short || other is Short -> when {
            value.toShort() < other.toShort() -> -1
            value.toShort() == other.toShort() -> 0
            value.toShort() > other.toShort() -> 1
            else -> throw IllegalStateException("Unsupported operation")
        }
        value is Byte || other is Byte -> when {
            value.toByte() < other.toByte() -> -1
            value.toByte() == other.toByte() -> 0
            value.toByte() > other.toByte() -> 1
            else -> throw IllegalStateException("Unsupported operation")
        }
        else -> throw IllegalArgumentException("Unsupported type ${value::class} and ${other::class}")
    }

    override fun compare(p0: kotlin.Number, p1: kotlin.Number): Int = Number(p0).compareTo(p1)

    override fun equals(other: Any?): Boolean = when (other) {
        is kotlin.Number -> compare(value, other) == 0
        is Number -> compare(value, other.value) == 0
        else -> false
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = "$value"
}

suspend fun IMutableObservableBase<Any?, Number, IMutableObservableChangedScope<Number>, *>.incrementAndGet(): Number =
    changeValue { it + 1 }

suspend fun IMutableObservableBase<Any?, Number, IMutableObservableChangedScope<Number>, *>.decrementAndGet(): Number =
    changeValue { it - 1 }

suspend fun IMutableObservableBase<Any?, Number, IMutableObservableChangedScope<Number>, *>.getAndIncrement(): Number {
    lateinit var result: Number
    changeValue { it.also { result = it } + 1 }
    return result
}

suspend fun IMutableObservableBase<Any?, Number, IMutableObservableChangedScope<Number>, *>.getAndDecrement(): Number {
    lateinit var result: Number
    changeValue { it.also { result = it } - 1 }
    return result
}