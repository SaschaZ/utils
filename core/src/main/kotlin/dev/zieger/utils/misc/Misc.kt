package dev.zieger.utils.misc

import kotlin.reflect.KClass

/**
 * Can be used to handle a "else when null" case without the need of calling `.invoke()`.
 *
 * Example:
 * ```kotlin
 *  val testVal = someNullableValue?.let {
 *      it + 4
 *  } ?: {
 *      10
 *  }.invoke()
 * ```
 * becomes
 * ```kotlin
 *  val testVal = someNullableValue?.let {
 *      it + 4
 *  } ifNull {
 *      10
 *  }
 * ```
 */
inline infix fun <R> R?.ifNull(block: () -> R): R = this ?: block()

val <T : Any> KClass<T>.name: String
    get() = java.simpleName


fun <K, V> Iterable<Pair<K, V>>.toMap(merger: (V, V) -> V) =
    groupBy { it.first }.map { it.key to it.value.merge(merger) }.toMap()

fun <K, V> Iterable<Pair<K, V>>.merge(merger: (V, V) -> V): V {
    var value = first().second
    val list = toList()
    (1..list.lastIndex).forEach { value = merger(value, list[it].second) }
    return value
}

fun <T : Comparable<T>> min(vararg values: T?): T = minOrNull(*values)!!

fun <T : Comparable<T>> minOrNull(vararg values: T?): T? {
    var min: T? = null
    values.filterNotNull().forEach { v -> min = if (min?.let { it > v } != false) v else min }
    return min
}

fun <T : Comparable<T>> max(vararg values: T?): T = maxOrNull(*values)!!

fun <T : Comparable<T>> maxOrNull(vararg values: T?): T? {
    var max: T? = null
    values.filterNotNull().forEach { v -> max = if (max?.let { it < v } != false) v else max }
    return max
}