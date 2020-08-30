@file:Suppress("FunctionName")

package dev.zieger.utils.core_testing.assertion

import dev.zieger.utils.core_testing.assertion2.rem


infix fun <A : Any?> A.assert(expected: Regex) =
    AssertRegexScope(expected, ActualMessageScope(toString())).apply { validate() }

operator fun <A : Any?, E : Any?> E.rem(other: String): Pair<E, IValidationScope<A, E>.() -> String> = rem { other }
operator fun <A : Any?, E : Any?> E.rem(other: IValidationScope<A, E>.() -> String) = this to other

infix fun <A : Any?, E : Any?> A.assert(expected: E) =
    AssertEqualsScope(expected, ActualMessageScope(this)).apply { validate() }

infix fun <A : Any?, E : Any?> A.assert(expected: Pair<E, IValidationScope<A, E>.() -> String>) =
    AssertEqualsScope(expected.first, ActualMessageScope(this, expected.second)).apply { validate() }

object Tester {
    @JvmStatic
    fun main(args: Array<String>) {
        val test = 6
        test assert 6
        test assert 45 % "foo"
        test assert 5 % { "foo$expected$actual" }

        val str = "foo"
        str assert Regex("boo")
        str assert "foo"
        str assert Regex("") % "moo"
        str assert "foo" % "moo"
    }
}