package de.gapps.utils.core_testing.assertion

interface ActualExpectedScope<A : Any?, E : Any?> {

    var actual: A
    var expected: E
}