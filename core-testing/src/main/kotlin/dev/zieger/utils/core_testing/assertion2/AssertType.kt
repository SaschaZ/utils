@file:Suppress("ClassName", "unused", "UNCHECKED_CAST")

package dev.zieger.utils.core_testing.assertion2

import dev.zieger.utils.misc.name
import dev.zieger.utils.misc.whenNotNull

sealed class AssertType(val assert: (Any?, Any?) -> Boolean) {
    object NULL : AssertType({ v, _ -> v == null })
    object NOT_NULL : AssertType({ v, _ -> v != null })
    object EQUALS : AssertType({ a, e -> a == e })
    object NOT_EQUALS : AssertType({ a, e -> a != e })
    object MATCHES : AssertType({ a, e -> (e as Regex).matches(a.toString()) })
    object NOT_MATCHES : AssertType({ a, e -> !(e as Regex).matches(a.toString()) })
    object GREATER : AssertType({ a, e -> a compare e > 0 })
    object GREATER_OR_EQUAL : AssertType({ a, e -> a compare e >= 0 })
    object SMALLER : AssertType({ a, e -> a compare e < 0 })
    object SMALLER_OR_EQUAL : AssertType({ a, e -> a compare e <= 0 })

    open class CUSTOM(assert: (Any?, Any?) -> Boolean) : AssertType(assert)

    override fun toString(): String = this::class.name
}

infix fun Any?.compare(other: Any?): Int {
    return whenNotNull(this, other) { t, o ->
        when {
            t is Comparable<*> && t is Number && o is Number -> (t as Comparable<Number>).compareTo(o)
            t is Comparable<*> && t is String && o is String -> (t as Comparable<String>).compareTo(o)
            else -> null
        }
    } ?: throw IllegalArgumentException("Can not compare null type")
}