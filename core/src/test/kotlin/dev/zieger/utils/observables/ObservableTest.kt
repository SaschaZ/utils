@file:Suppress("unused")

package dev.zieger.utils.observables

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.onFail
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log.Log
import dev.zieger.utils.observable.Observable
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test

class ObservableTest {

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