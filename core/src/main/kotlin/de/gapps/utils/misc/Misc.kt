package de.gapps.utils.misc

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