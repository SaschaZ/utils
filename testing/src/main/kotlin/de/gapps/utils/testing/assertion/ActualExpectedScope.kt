package de.gapps.utils.testing.assertion

interface ActualExpectedScope<A : Any, E : Any> {

    var actual: A
    var expected: E
}