@file:Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter")

package dev.zieger.utils.observables

import dev.zieger.utils.core_testing.*
import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.observable.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test

class ObservableTest {

    @Test
    fun `test observable inside class`() = runTest {
        parameterMix(
            { TestClass("foo", it) },
            param("storePreviousValues", true, false),
            param("notifyForExisting", true, false),
            param("notifyOnChangedOnly", true, false),
            param("scope", DefaultCoroutineScope(), null),
            param("mutex", Mutex(), null)
        ) {
//            println(this)

            val testClass = this
            testClass.observable.value assert "foo" % "initial observable.value"
            testClass.value assert "foo" % "initial value"

            var latestObservedChanged: String? = null
            testClass.observable.observe { Log.d("observe -> value=$it"); latestObservedChanged = it }
            latestObservedChanged assert (if (notifyForExisting) "foo" else null) % "after observe without changes"

            var latestObservedChangedS: String? = null
            testClass.observable.observeS { Log.d("observeS -> value=$it"); latestObservedChangedS = it }
            delay(10L)
            latestObservedChangedS assert (if (notifyForExisting && scope != null) "foo" else null) % "after observe without changes suspended"

            testClass.value = "boo"
            delay(10L)

            testClass.observable.value assert "boo" % "after change observable.value"
            testClass.value assert "boo" % "after change value"
            latestObservedChanged assert "boo" % "after change observe"
            latestObservedChangedS assert (if (scope != null) "boo" else null) % "after change observe suspended"
        }
    }

    companion object {

        data class TestClass(
            val initial: String,
            val map: Map<String, ParamInstance<*>>
        ) {

            val storePreviousValues: Boolean by bind(map)
            val notifyForExisting: Boolean by bind(map)
            val notifyOnChangedOnly: Boolean by bind(map)
            val scope: CoroutineScope? by bind(map)
            val mutex: Mutex? by bind(map)

            val observable =
                Observable(initial, notifyOnChangedOnly, notifyForExisting, storePreviousValues, scope, mutex)
            var value by observable
        }
    }
}