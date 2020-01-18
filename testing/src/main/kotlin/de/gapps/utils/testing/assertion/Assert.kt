package de.gapps.utils.testing.assertion


infix fun String.assert(regex: Regex) = StringRegexScope(regex).apply { assertInternal() }
infix fun StringRegexScope.assert(actual: String) = actual.assertInternal()

infix fun <T : Any> T.assert(expected: T) = AssertScope(expected).apply { assertInternal() }
infix fun <A : Any> AssertScope<A>.assert(actual: A) = actual.assertInternal()

infix fun <T : Any> T.onFail(message: String) = onFail { message }
infix fun <T : Any> T.onFail(message: ActualExpectedScope<T, *>.() -> String) = MessageScope(message)

private fun testIt() {
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