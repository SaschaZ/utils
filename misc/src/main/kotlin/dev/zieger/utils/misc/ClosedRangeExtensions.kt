package dev.zieger.utils.misc

fun <T> ClosedRange<T>.range(): Double where T : Number, T : Comparable<T> =
    endInclusive.toDouble() - start.toDouble()

fun <T> ClosedRange<T>.toInt(): ClosedRange<Int> where T : Number, T : Comparable<T> =
    start.toInt()..endInclusive.toInt()

fun <T> ClosedRange<T>.toLong(): ClosedRange<Long> where T : Number, T : Comparable<T> =
    start.toLong()..endInclusive.toLong()

fun <T> ClosedRange<T>.toFloat(): ClosedRange<Float> where T : Number, T : Comparable<T> =
    start.toFloat()..endInclusive.toFloat()

fun <T> ClosedRange<T>.toDouble(): ClosedRange<Double> where T : Number, T : Comparable<T> =
    start.toDouble()..endInclusive.toDouble()