package dev.zieger.utils.core_testing.assertion2

import dev.zieger.utils.misc.nullWhenBlank
import junit.framework.TestCase

infix fun Any?.assert2(type: AssertType) = assert2(type to "")

infix fun Any?.assert2(type: Pair<AssertType, String>) {
    fun buildMessage(actual: Any?, expected: Any? = null): String =
        (type.second.nullWhenBlank()?.let { "message: $it\n" } ?: "") +
                "type: ${type.first}\n" + when (expected) {
            null -> "actual:<$actual>"
            else -> "expected:<$expected> but was:<$actual>"
        }

    when (this) {
        is Pair<*, *> -> if (!type.first.assert(first, second))
            TestCase.fail(buildMessage(first, second)) else Unit

        else -> if (!type.first.assert(this, null))
            TestCase.fail(buildMessage(this)) else Unit
    }
}

operator fun <A : Any> A.rem(message: String) = this to message