@file:Suppress("unused")

package de.gapps.utils.observables

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.rem
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.log.Log
import de.gapps.utils.observable.Observable
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test

class ObservableTest {

    @Test
    fun `test observable inside class`() = runTest {
        val testClass = TestClass("foo")
        testClass.observable.value assert "foo" % "1"
        var latestObservedChanged: String = testClass.observable.value
        launchEx { testClass.observable.observe { Log.d("observe; value=$it"); latestObservedChanged = it } }
        delay(100L)
        launchEx { testClass.triggerChange("boo") }
        delay(100L)

        testClass.observable.value assert "boo" % "2"
        latestObservedChanged assert "boo" % "3"
        testClass.success assert true % "4"
    }

    companion object {

        class TestClass(value: String) {
            var success: Boolean = false
                private set
            val observable = Observable(value)
            private var internal by observable.apply {
                observe { success = true }
            }

            fun triggerChange(value: String) {
                internal = value
            }
        }
    }
}