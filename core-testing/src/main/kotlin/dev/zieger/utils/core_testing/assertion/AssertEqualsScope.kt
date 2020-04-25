package dev.zieger.utils.core_testing.assertion

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue

class AssertEqualsScope<A : Any?, E : Any?>(
    override var expected: E,
    scope: ActualMessageScope<A, E>
) : IValidationScope<A, E>, IActualMessageScope<A, E> by scope {
    override fun validate() {
        assertEquals(message(), expected, actual)
    }
}

class AssertRegexScope(
    private val regex: Regex,
    scope: ActualMessageScope<String, Regex>
) : IValidationScope<String, Regex>, IActualMessageScope<String, Regex> by scope {
    override var expected: Regex = regex
    override fun validate() {
        assertTrue(message(), actual.matches(regex))
    }
}