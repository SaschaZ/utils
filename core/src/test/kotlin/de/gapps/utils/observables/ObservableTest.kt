@file:Suppress("unused")

package de.gapps.utils.observables

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.observable.Observable
import de.gapps.utils.testing.assertion.assert
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ObservableTest {

    @Test
    fun `test observable inside class`() = runBlocking {
        val testClass = TestClass("foo")
        testClass.observable.value assert "foo"
        var latestObservedChanged: String = testClass.observable.value
        launchEx { testClass.observable.observe { latestObservedChanged = it } }

        launchEx { testClass.triggerChange("boo") }
        delay(100L)

        testClass.observable.value assert "boo"
        latestObservedChanged assert "boo"
        testClass.success assert true
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