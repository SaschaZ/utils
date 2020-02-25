@file:Suppress("unused")

package de.gapps.utils.observables

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.log.Log
import de.gapps.utils.observable.Observable
import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.assertion.onFail
import de.gapps.utils.testing.runTest
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.delay

class ObservableTest : AnnotationSpec() {

    @Test
    fun `test observable inside class`() = runTest {
        val testClass = TestClass("foo")
        testClass.observable.value onFail "1" assert "foo"
        var latestObservedChanged: String = testClass.observable.value
        launchEx { testClass.observable.observe { Log.d("observe; value=$it"); latestObservedChanged = it } }
        delay(100L)
        launchEx { testClass.triggerChange("boo") }
        delay(100L)

        testClass.observable.value onFail "2" assert "boo"
        latestObservedChanged onFail "3" assert "boo"
        testClass.success onFail "4" assert true
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