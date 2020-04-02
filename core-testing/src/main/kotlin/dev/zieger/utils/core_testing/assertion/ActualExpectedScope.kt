package dev.zieger.utils.core_testing.assertion

interface ActualExpectedScope<A : Any?, E : Any?> {

    var actual: A
    var expected: E
}