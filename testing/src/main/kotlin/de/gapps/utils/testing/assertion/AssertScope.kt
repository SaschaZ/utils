package de.gapps.utils.testing.assertion

class AssertScope<T : Any>(
    override var expected: T,
    scope: MessageScope<T> = MessageScope()
) : IValidationScope<T, T>, IMessageScope<T> by scope {

    override fun T.assertInternal() {
        actual = this
        kotlin.test.assertEquals(actual, expected, message = message())
    }
}