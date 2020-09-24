@file:Suppress("ClassName", "unused", "UNCHECKED_CAST")

package dev.zieger.utils.core_testing.assertion2

import dev.zieger.utils.core_testing.assertion2.AssertType.*
import dev.zieger.utils.misc.catch
import dev.zieger.utils.misc.name
import dev.zieger.utils.misc.whenNotNull
import kotlin.reflect.KClass

infix fun Any?.isNull(msg: AssertionScope.() -> String) = this assert2 (NULL to msg)
fun Any?.isNull() = isNull { "" }
infix fun Any?.isNull(msg: String) = isNull { msg }
fun Any?.isNotNull() = isNotNull { "" }
infix fun Any?.isNotNull(msg: AssertionScope.() -> String) = this assert2 (NOT_NULL to msg)
infix fun Any?.isNotNull(msg: String) = isNotNull { msg }

fun Any?.isTrue() = isTrue { "" }
infix fun Any?.isTrue(msg: AssertionScope.() -> String) = this assert2 (TRUE to msg)
infix fun Any?.isTrue(msg: String) = isTrue { msg }
fun Any?.isTrueOrNull() = isTrue { "" }
infix fun Any?.isTrueOrNull(msg: AssertionScope.() -> String) = this assert2 (TRUE_OR_NULL to msg)
infix fun Any?.isTrueOrNull(msg: String) = isTrue { msg }

fun Any?.isFalse() = isFalse { "" }
infix fun Any?.isFalse(msg: AssertionScope.() -> String) = this assert2 (FALSE to msg)
infix fun Any?.isFalse(msg: String) = isFalse { msg }
fun Any?.isFalseOrNull() = isFalse { "" }
infix fun Any?.isFalseOrNull(msg: AssertionScope.() -> String) = this assert2 (FALSE_OR_NULL to msg)
infix fun Any?.isFalseOrNull(msg: String) = isFalse { msg }

infix fun Any?.isEqual(o: Any?) = this to o assert2 EQUALS
infix fun Any?.isEqual(o: Pair<Any?, AssertionScope.() -> String>) = this to o.first assert2 (EQUALS to o.second)
infix fun Any?.isEqualOrNull(o: Any?) = this to o assert2 EQUALS_OR_NULL
infix fun Any?.isEqualOrNull(o: Pair<Any?, AssertionScope.() -> String>) =
    this to o.first assert2 (EQUALS_OR_NULL to o.second)

infix fun Any?.isSame(o: Any?) = this to o assert2 SAME
infix fun Any?.isSame(o: Pair<Any?, AssertionScope.() -> String>) = this to o.first assert2 (SAME to o.second)
infix fun Any?.isSameOrNull(o: Any?) = this to o assert2 SAME_OR_NULL
infix fun Any?.isSameOrNull(o: Pair<Any?, AssertionScope.() -> String>) =
    this to o.first assert2 (SAME_OR_NULL to o.second)

infix fun Any?.isNotEqual(o: Any?) = this to o assert2 NOT_EQUALS
infix fun Any?.isNotEqual(o: Pair<Any?, AssertionScope.() -> String>) = this to o.first assert2 (NOT_EQUALS to o.second)
infix fun Any?.isNotEqualOrNull(o: Any?) = this to o assert2 NOT_EQUALS_OR_NULL
infix fun Any?.isNotEqualOrNull(o: Pair<Any?, AssertionScope.() -> String>) =
    this to o.first assert2 (NOT_EQUALS_OR_NULL to o.second)

infix fun Any?.isAnyOf(o: List<Any?>) = this to o assert2 ANY_OF
infix fun Any?.isAnyOf(o: Pair<List<Any?>, AssertionScope.() -> String>) = this to o assert2 ANY_OF
infix fun Any?.isAnyOfOrNull(o: List<Any?>) = this to o assert2 ANY_OF_OR_NULL
infix fun Any?.isAnyOfOrNull(o: Pair<List<Any?>, AssertionScope.() -> String>) = this to o assert2 ANY_OF_OR_NULL

infix fun Any?.isNoneOf(o: List<Any?>) = this to o assert2 NONE_OF
infix fun Any?.isNoneOf(o: Pair<List<Any?>, AssertionScope.() -> String>) = this to o assert2 NONE_OF
infix fun Any?.isNoneOfOrNull(o: List<Any?>) = this to o assert2 NONE_OF_OR_NULL
infix fun Any?.isNoneOfOrNull(o: Pair<List<Any?>, AssertionScope.() -> String>) = this to o assert2 NONE_OF_OR_NULL

infix fun Any?.isMatching(o: String) = this to o.toRegex() assert2 MATCHES
infix fun Any?.isMatching(o: Pair<String, AssertionScope.() -> String>) =
    this to o.first.toRegex() assert2 (MATCHES to o.second)

infix fun Any?.isMatchingOrNull(o: String) = this to o.toRegex() assert2 MATCHES_OR_NULL
infix fun Any?.isMatchingOrNull(o: Pair<String, AssertionScope.() -> String>) =
    this to o.first.toRegex() assert2 (MATCHES_OR_NULL to o.second)

infix fun Any?.isNotMatching(o: String) = this to o.toRegex() assert2 NOT_MATCHES
infix fun Any?.isNotMatching(o: Pair<String, AssertionScope.() -> String>) =
    this to o.first.toRegex() assert2 (NOT_MATCHES to o.second)

infix fun Any?.isNotMatchingOrNull(o: String) = this to o.toRegex() assert2 NOT_MATCHES_OR_NULL
infix fun Any?.isNotMatchingOrNull(o: Pair<String, AssertionScope.() -> String>) =
    this to o.first.toRegex() assert2 (NOT_MATCHES_OR_NULL to o.second)

infix fun <T : Comparable<T>> T?.isGreater(o: T?) = this to o assert2 GREATER
infix fun <T : Comparable<T>> T?.isGreater(o: Pair<T?, AssertionScope.() -> String>) =
    this to o.first assert2 (GREATER to o.second)

infix fun <T : Comparable<T>> T?.isGreaterOrNull(o: T?) = this to o assert2 GREATER_OR_NULL
infix fun <T : Comparable<T>> T?.isGreaterOrNull(o: Pair<T?, AssertionScope.() -> String>) =
    this to o.first assert2 (GREATER_OR_NULL to o.second)

infix fun <T : Comparable<T>> T?.isGreaterOrEqual(o: T?) = this to o assert2 GREATER_OR_EQUAL
infix fun <T : Comparable<T>> T?.isGreaterOrEqual(o: Pair<T?, AssertionScope.() -> String>) =
    this to o.first assert2 (GREATER_OR_EQUAL to o.second)

infix fun <T : Comparable<T>> T?.isGreaterOrEqualOrNull(o: T?) = this to o assert2 GREATER_OR_EQUAL_OR_NULL
infix fun <T : Comparable<T>> T?.isGreaterOrEqualOrNull(o: Pair<T?, AssertionScope.() -> String>) =
    this to o.first assert2 (GREATER_OR_EQUAL_OR_NULL to o.second)

infix fun <T : Comparable<T>> T?.isSmaller(o: T?) = this to o assert2 SMALLER
infix fun <T : Comparable<T>> T?.isSmaller(o: Pair<T?, AssertionScope.() -> String>) =
    this to o.first assert2 (SMALLER to o.second)

infix fun <T : Comparable<T>> T?.isSmallerOrNull(o: T?) = this to o assert2 SMALLER_OR_NULL
infix fun <T : Comparable<T>> T?.isSmallerOrNull(o: Pair<T?, AssertionScope.() -> String>) =
    this to o.first assert2 (SMALLER_OR_NULL to o.second)

infix fun <T : Comparable<T>> T?.isSmallerOrEqual(o: T?) = this to o assert2 SMALLER_OR_EQUAL
infix fun <T : Comparable<T>> T?.isSmallerOrEqual(o: Pair<T?, AssertionScope.() -> String>) =
    this to o.first assert2 (SMALLER_OR_EQUAL to o.second)

infix fun <T : Comparable<T>> T?.isSmallerOrEqualOrNull(o: T?) = this to o assert2 SMALLER_OR_EQUAL_OR_NULL
infix fun <T : Comparable<T>> T?.isSmallerOrEqualOrNull(o: Pair<T?, AssertionScope.() -> String>) =
    this to o.first assert2 (SMALLER_OR_EQUAL_OR_NULL to o.second)

infix fun <T : Comparable<T>> T?.isInRange(o: ClosedRange<T>?) = this to o assert2 RANGE<T>()
infix fun <T : Comparable<T>> T?.isInRange(o: Pair<ClosedRange<T>?, AssertionScope.() -> String>) =
    this to o.first assert2 (RANGE<T>() to o.second)

infix fun <T : Comparable<T>> T?.isInRangeOrNull(o: ClosedRange<T>?) = this to o assert2 RANGE_OR_NULL<T>()
infix fun <T : Comparable<T>> T?.isInRangeOrNull(o: Pair<ClosedRange<T>?, AssertionScope.() -> String>) =
    this to o.first assert2 (RANGE_OR_NULL<T>() to o.second)

infix fun <T : Any> Any?.isInstance(o: KClass<T>?) = this to o assert2 INSTANCE<T>()
infix fun <T : Any> Any?.isInstance(o: Pair<KClass<T>?, AssertionScope.() -> String>) =
    this to o.first assert2 (INSTANCE<T>() to o.second)

infix fun <T : Any> Any?.isInstanceOrNull(o: KClass<T>?) = this to o assert2 INSTANCE_OR_NULL<T>()
infix fun <T : Any> Any?.isInstanceOrNull(o: Pair<KClass<T>?, AssertionScope.() -> String>) =
    this to o.first assert2 (INSTANCE_OR_NULL<T>() to o.second)

infix fun List<*>?.hasSameContent(o: List<*>?) = this to o assert2 SAME_CONTENT
infix fun List<*>?.hasSameContent(o: Pair<List<*>?, AssertionScope.() -> String>) =
    this to o.first assert2 (SAME_CONTENT to o.second)

infix fun (() -> Unit).isThrowing(t: KClass<out Throwable>) = this to t assert2 THROWS
infix fun (() -> Unit).isThrowing(t: Pair<KClass<out Throwable>, AssertionScope.() -> String>) =
    this to t.first assert2 (THROWS to t.second)

sealed class AssertType(val assert: (Any?, Any?) -> Boolean) {
    object NULL : AssertType({ v, _ -> v == null })
    object NOT_NULL : AssertType({ v, _ -> v != null })

    object TRUE : AssertType({ v, _ -> v == true })
    object TRUE_OR_NULL : AssertType({ v, _ -> v == null || v == true })

    object FALSE : AssertType({ v, _ -> v == false })
    object FALSE_OR_NULL : AssertType({ v, _ -> v == null || v == false })

    object EQUALS : AssertType({ a, e -> a == e })
    object EQUALS_OR_NULL : AssertType({ a, e -> a == null || a == e })

    object SAME : AssertType({ a, e -> a === e })
    object SAME_OR_NULL : AssertType({ a, e -> a == null || a === e })

    object NOT_EQUALS : AssertType({ a, e -> a != e })
    object NOT_EQUALS_OR_NULL : AssertType({ a, e -> a == null || a != e })

    object ANY_OF : AssertType({ a, e -> (e as? List<Any?>)?.contains(a) == true })
    object ANY_OF_OR_NULL : AssertType({ a, e -> a == null || (e as? List<Any?>)?.contains(a) == true })

    object NONE_OF : AssertType({ a, e -> (e as? List<Any?>)?.contains(a) == false })
    object NONE_OF_OR_NULL : AssertType({ a, e -> a == null || (e as? List<Any?>)?.contains(a) == false })

    object MATCHES : AssertType({ a, e -> (e as Regex).matches(a.toString()) })
    object MATCHES_OR_NULL : AssertType({ a, e -> a == null || (e as Regex).matches(a.toString()) })

    object NOT_MATCHES : AssertType({ a, e -> !(e as Regex).matches(a.toString()) })
    object NOT_MATCHES_OR_NULL : AssertType({ a, e -> a == null || !(e as Regex).matches(a.toString()) })

    object GREATER : AssertType({ a, e -> a compare e > 0 })
    object GREATER_OR_NULL : AssertType({ a, e -> a == null || a compare e > 0 })

    object GREATER_OR_EQUAL : AssertType({ a, e -> a compare e >= 0 })
    object GREATER_OR_EQUAL_OR_NULL : AssertType({ a, e -> a == null || a compare e >= 0 })

    object SMALLER : AssertType({ a, e -> a compare e < 0 })
    object SMALLER_OR_NULL : AssertType({ a, e -> a == null || a compare e < 0 })

    object SMALLER_OR_EQUAL : AssertType({ a, e -> a compare e <= 0 })
    object SMALLER_OR_EQUAL_OR_NULL : AssertType({ a, e -> a == null || a compare e <= 0 })

    class RANGE<T : Comparable<T>> : AssertType({ a, e -> (a as T) in (e as ClosedRange<T>) })
    class RANGE_OR_NULL<T : Comparable<T>> : AssertType({ a, e -> a == null || (a as T) in (e as ClosedRange<T>) })

    class INSTANCE<T : Any> : AssertType({ a, e -> (e as KClass<T>).isInstance(a as T) })
    class INSTANCE_OR_NULL<T : Any> : AssertType({ a, e -> a == null || (e as KClass<T>).isInstance(a as T) })

    object SAME_CONTENT : AssertType({ a, e ->
        val exp = (e as? List<*>)
        (a as? List<*>)?.mapIndexed { index, value -> exp?.getOrNull(index) == value }?.all { it } == true
    })

    object THROWS : AssertType({ a, e ->
        var succeed = false
        catch(true, onCatch = { succeed = (e as KClass<out Throwable>).isInstance(it) }) { (a as () -> Unit)() }
        succeed
    })

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