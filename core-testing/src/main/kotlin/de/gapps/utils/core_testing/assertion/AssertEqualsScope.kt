package de.gapps.utils.core_testing.assertion

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

class AssertEqualsScope<T : Any?>(
    override var expected: T,
    scope: ActualMessageScope<T>
) : IValidationScope<T, T>, IActualMessageScope<T> by scope {
    override fun validate() {
        assertEquals(message(), expected, actual)
    }
}

class AssertRegexScope(
    private val regex: Regex,
    scope: ActualMessageScope<String>
) : IValidationScope<String, String>, IActualMessageScope<String> by scope {
    override var expected: String = regex.pattern
    override fun validate() {
        assertTrue(message(), actual.matches(regex))
    }
}