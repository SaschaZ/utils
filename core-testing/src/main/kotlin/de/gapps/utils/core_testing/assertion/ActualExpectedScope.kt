package de.gapps.utils.core_testing.assertion

interface ActualExpectedScope<out A : Any?, out E : Any?> {

    var actual: @UnsafeVariance A
    var expected: @UnsafeVariance E
}