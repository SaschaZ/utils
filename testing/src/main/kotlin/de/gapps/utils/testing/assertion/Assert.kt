package de.gapps.utils.testing.assertion


infix fun String.assert(expected: Regex) =
    AssertRegexScope(expected, ActualMessageScope(this)).apply { validate() }

infix fun <T : Any> T.onFail(message: String) = onFail { message }
infix fun <T : Any> T.onFail(message: IValidationScope<T, T>.() -> String) = ActualMessageScope(this, message)

infix fun <T : Any> T.assert(expected: T) =
    AssertEqualsScope(expected, ActualMessageScope(this)).apply { validate() }

infix fun <T : Any> ActualMessageScope<T>.assert(expected: T) =
    AssertEqualsScope(expected, this).validate()

object Tester {
    @JvmStatic
    fun main(args: Array<String>) {
        val test = 6
        test assert 6
        test onFail "foo" assert 45
        test onFail { "foo$expected$actual" } assert 5

        val str = "foo"
        str assert Regex("boo")
        str assert "foo"
        str onFail "moo" assert Regex("")
        str onFail "moo" assert "foo"
    }
}