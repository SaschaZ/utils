package dev.zieger.utils.misc

inline fun <reified T> ClosedRange<T>.range(): T where T : Number, T : Comparable<T> = when (T::class) {
    Double::class -> endInclusive.toDouble() - start.toDouble()
    Float::class -> endInclusive.toFloat() - start.toFloat()
    Long::class -> endInclusive.toLong() - start.toLong()
    Int::class -> endInclusive.toInt() - start.toInt()
    Byte::class -> endInclusive.toByte() - start.toByte()
    else -> throw IllegalArgumentException("Unknown Number typ: ${T::class}")
} as T

fun <T> ClosedRange<T>.toInt(): ClosedRange<Int> where T : Number, T : Comparable<T> =
    start.toInt()..endInclusive.toInt()

fun <T> ClosedRange<T>.toLong(): ClosedRange<Long> where T : Number, T : Comparable<T> =
    start.toLong()..endInclusive.toLong()

fun <T> ClosedRange<T>.toFloat(): ClosedRange<Float> where T : Number, T : Comparable<T> =
    start.toFloat()..endInclusive.toFloat()

fun <T> ClosedRange<T>.toDouble(): ClosedRange<Double> where T : Number, T : Comparable<T> =
    start.toDouble()..endInclusive.toDouble()