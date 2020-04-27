@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.observables

import dev.zieger.utils.core_testing.*
import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.observable.Controllable
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test

class ControllableTest {

    @Test
    fun `test controllable`() = runTest {
        val testObservable = Controllable("foo")
        testObservable.value assert "foo" % "1"

        testObservable.value = "boo"
        delay(100L)

        testObservable.value assert "boo" % "2"
    }

    @Test
    fun `test controllable inside class`() = runTest(30.seconds) {
        parameterMix(
            { TestClass("foo", it) },
            param("storePreviousValues", true, false),
            param("notifyForExisting", true, false),
            param("notifyOnChangedOnly", true, false),
            param("scope", DefaultCoroutineScope(), null),
            param("mutex", Mutex(), null)
        ) {
            println(this)
            val testClass = this

            testClass.controllable.control { Log.d("111=$it"); if (value == "foo") value = "boo" }
            delay(100L)
            testClass.controllable.value assert (if (notifyForExisting) "boo" else "foo") % "1"
            testClass.value assert (if (notifyForExisting) "boo" else "foo") % "2"

            testClass.value = "moo"
            delay(100L)
            testClass.value assert "moo" % "3"
            testClass.controllable.value assert "moo" % "4"
        }
    }

    @Test
    fun `test controllable inside class suspended`() = runTest(30.seconds) {
        parameterMix(
            { TestClass("foo", it) },
            param("storePreviousValues", true, false),
            param("notifyForExisting", true, false),
            param("notifyOnChangedOnly", true, false),
            param("scope", DefaultCoroutineScope(), null),
            param("mutex", Mutex(), null)
        ) {
            println(this)
            if (scope != null) {
                val testClass = this

                testClass.controllable.controlS { Log.d("111=$it"); if (value == "foo") value = "boo" }
                delay(10L)
                testClass.controllable.value assert (if (notifyForExisting) "boo" else "foo") % "1"
                testClass.value assert (if (notifyForExisting) "boo" else "foo") % "2"

                testClass.value = "moo"
                delay(10L)
                testClass.value assert "moo" % "3"
                testClass.controllable.value assert "moo" % "4"
            }
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

            val controllable = Controllable(
                initial,
                notifyOnChangedOnly,
                notifyForExisting,
                storePreviousValues,
                scope,
                mutex
            )
            var value by controllable
        }
    }
}