package de.gapps.utils.testing.assertion

class StringRegexScope(
    override var expected: Regex,
    scope: MessageScope<String> = MessageScope()
) : IValidationScope<String, Regex>, IMessageScope<String> by scope {

    override fun String.assertInternal() {
        actual = this
        kotlin.test.assertTrue(matches(expected), message = message())
    }
}