package de.gapps.utils.testing.assertion

import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssertEqualsScope<T : Any?>(
    override var expected: T,
    scope: ActualMessageScope<T>
) : IValidationScope<T, T>, IActualMessageScope<T> by scope {
    override fun validate() {
        assertEquals(expected, actual, message())
    }
}

class AssertRegexScope(
    private val regex: Regex,
    scope: ActualMessageScope<String>
) : IValidationScope<String, String>, IActualMessageScope<String> by scope {
    override var expected: String = regex.pattern
    override fun validate() {
        assertTrue(actual.matches(regex), message())
    }
}