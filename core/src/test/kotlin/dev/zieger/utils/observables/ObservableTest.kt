@file:Suppress("unused")

package dev.zieger.utils.observables

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.observable.Observable
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test

class ObservableTest {

    @Test
    fun `test observable inside class`() = runTest {
        val testClass = TestClass("foo")
        testClass.observable.value assert "foo" % "1"
        testClass.value assert "foo" % "1b"

        var latestObservedChanged: String = testClass.observable.value
        testClass.observable.observe { Log.d("observe; value=$it"); latestObservedChanged = it }
        var latestObservedChangedS: String = testClass.observable.value
        testClass.observable.observeS { Log.d("observeS; value=$it"); latestObservedChangedS = it }
        testClass.value = "boo"
        delay(200L)

        testClass.observable.value assert "boo" % "2"
        testClass.value assert "boo" % "2b"
        latestObservedChanged assert "boo" % "3"
        latestObservedChangedS assert "boo" % "3"
    }

    companion object {

        class TestClass(value: String) {
            val observable = Observable(value, scope = DefaultCoroutineScope())
            var value by observable
        }
    }
}