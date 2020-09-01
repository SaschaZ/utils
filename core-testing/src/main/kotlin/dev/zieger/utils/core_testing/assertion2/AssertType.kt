@file:Suppress("ClassName", "unused", "UNCHECKED_CAST")

package dev.zieger.utils.core_testing.assertion2

import dev.zieger.utils.core_testing.assertion2.AssertType.*
import dev.zieger.utils.misc.name
import dev.zieger.utils.misc.whenNotNull

val Any?.isNull get() = this assert NULL
infix fun Any?.isNull(msg: String) = this assert NULL % msg
val Any?.isNotNull get() = this assert NOT_NULL
infix fun Any?.isNotNull(msg: String) = this assert NULL % msg
val Any?.isTrue get() = this assert TRUE
infix fun Any?.isTrue(msg: String) = this assert TRUE % msg
val Any?.isFalse get() = this assert FALSE
infix fun Any?.isFalse(msg: String) = this assert FALSE % msg

infix fun Any?.isEqual(o: Any?) = this to o assert EQUALS
infix fun Any?.isEqual(o: Pair<Any?, String>) = this to o.first assert EQUALS % o.second
infix fun Any?.isNotEqual(o: Any?) = this to o assert NOT_EQUALS
infix fun Any?.isNotEqual(o: Pair<Any?, String>) = this to o.first assert NOT_EQUALS % o.second
infix fun Any?.isMatching(o: String) = this to o.toRegex() assert MATCHES
infix fun Any?.isMatching(o: Pair<String, String>) = this to o.first.toRegex() assert MATCHES % o.second
infix fun Any?.isNotMatching(o: String) = this to o.toRegex() assert NOT_MATCHES
infix fun Any?.isNotMatching(o: Pair<String, String>) = this to o.first.toRegex() assert NOT_MATCHES % o.second
infix fun Any?.isGreater(o: Any?) = this to o assert GREATER
infix fun Any?.isGreater(o: Pair<Any?, String>) = this to o.first assert GREATER % o.second
infix fun Any?.isGreaterOrEqual(o: Any?) = this to o assert GREATER_OR_EQUAL
infix fun Any?.isGreaterOrEqual(o: Pair<Any?, String>) = this to o.first assert GREATER_OR_EQUAL % o.second
infix fun Any?.isSmaller(o: Any?) = this to o assert SMALLER
infix fun Any?.isSmaller(o: Pair<Any?, String>) = this to o.first assert SMALLER % o.second
infix fun Any?.isSmallerOrEqual(o: Any?) = this to o assert SMALLER_OR_EQUAL
infix fun Any?.isSmallerOrEqual(o: Pair<Any?, String>) = this to o.first assert SMALLER_OR_EQUAL % o.second

sealed class AssertType(val assert: (Any?, Any?) -> Boolean) {
    object NULL : AssertType({ v, _ -> v == null })
    object NOT_NULL : AssertType({ v, _ -> v != null })
    object TRUE : AssertType({ v, _ -> v == true })
    object FALSE : AssertType({ v, _ -> v == false })
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