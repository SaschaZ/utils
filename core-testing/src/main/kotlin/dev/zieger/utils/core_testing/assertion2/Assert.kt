package dev.zieger.utils.core_testing.assertion2

infix fun Any?.assert(type: AssertType) = assert(type to "")

infix fun Any?.assert(type: Pair<AssertType, String>) {
    when (this) {
        is Pair<*, *> -> if (!type.first.assert(first, second))
            throw UtilsAssertException(type.first, type.second, first, second) else Unit

        else -> if (!type.first.assert(this, null))
            throw UtilsAssertException(type.first, type.second, this) else Unit
    }
}

operator fun <A : Any> A.rem(message: String) = this to message