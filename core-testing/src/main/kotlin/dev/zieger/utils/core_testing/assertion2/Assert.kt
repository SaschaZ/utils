package dev.zieger.utils.core_testing.assertion2

import dev.zieger.utils.misc.nullWhenBlank
import junit.framework.AssertionFailedError

infix fun Any?.assert2(type: AssertType) {
    val lambda: AssertionScope.() -> String = { "" }
    assert2(type to lambda)
}

infix fun Any?.assert2(type: Pair<AssertType, AssertionScope.() -> String>) = type.let { (t, s) ->
    when (this) {
        is Pair<*, *> -> if (!t.assert(first, second))
            throw AssertionFailedError(buildMessage(t, s, first, second)) else Unit

        else -> if (!t.assert(this, null))
            throw AssertionFailedError(buildMessage(t, s, this)) else Unit
    }
}

private fun buildMessage(
    type: AssertType,
    message: (AssertionScope.() -> String)?,
    actual: Any?,
    expected: Any? = null
): String {
    val msg = message?.let { AssertionScope(actual, expected, type).it() }
    return (msg?.nullWhenBlank()?.let { "message: $it\n" } ?: "") +
            "assertion: $type\n" + when (expected) {
        null -> "actual:<$actual>"
        else -> "expected:<$expected> but was:<$actual>"
    }
}

data class AssertionScope(
    val actual: Any?,
    val expected: Any?,
    val assertType: AssertType
)

operator fun <A> A.rem(message: AssertionScope.() -> String) = this to message

operator fun <A> A.rem(message: String): Pair<A, AssertionScope.() -> String> {
    val lambda: AssertionScope.() -> String = { message }
    return this to lambda
}

fun <A> A.msg(message: AssertionScope.() -> String) = this to message

fun <A> A.msg(message: String): Pair<A, AssertionScope.() -> String> {
    val lambda: AssertionScope.() -> String = { message }
    return this to lambda
}