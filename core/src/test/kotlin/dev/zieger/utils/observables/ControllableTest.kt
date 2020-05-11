@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.observables

import dev.zieger.utils.core_testing.*
import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.observable.Controllable
import dev.zieger.utils.observable.Controllable2
import dev.zieger.utils.observable.IControllableBase
import dev.zieger.utils.observable.IControlledChangedScope2
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import org.junit.jupiter.api.Test

class ControllableTest {

    @Test
    fun `test controllable`() = runTest {
        val testControllable = Controllable("foo")
        testControllable.value assert "foo" % "1"

        testControllable.value = "boo"
        delay(100L)

        testControllable.value assert "boo" % "2"
    }

    @Test
    fun testControllable() = testControllable {
        TestClass("foo", it) { initial, notifyOnChangedOnly, notifyForExisting, storePreviousValues, scope, mutex ->
            Controllable(initial, notifyOnChangedOnly, notifyForExisting, storePreviousValues, scope, mutex)
        }
    }

    @Test
    fun testControllable2() = testControllable {
        TestClass("foo", it) { initial, notifyOnChangedOnly, notifyForExisting, storePreviousValues, scope, mutex ->
            Controllable2<Any?, String>(
                initial,
                notifyOnChangedOnly,
                notifyForExisting,
                storePreviousValues,
                scope,
                mutex
            )
        }
    }

    fun <S : IControlledChangedScope2<Any?, String>, C : IControllableBase<Any?, String, S>>
            testControllable(
        inputFactory: (Map<String, ParamInstance<*>>) -> TestClass<S, C>
    ) = runTest(30.seconds) {
        parameterMix(
            inputFactory,
            param("storePreviousValues", true, false),
            param("notifyForExisting", true, false),
            param("notifyOnChangedOnly", true, false),
            param("scope", DefaultCoroutineScope(), null),
            param("mutex", Mutex(), null)
        ) {
//            println(this)

            controllable.control { /*Log.d("111=$it");*/ if (value == "foo") value = "boo" }
            delay(10L)
            controllable.value assert (if (notifyForExisting) "boo" else "foo") % "1"
            value assert (if (notifyForExisting) "boo" else "foo") % "2"

            value = "moo"
            delay(10L)
            value assert "moo" % "3"
            controllable.value assert "moo" % "4"
        }
    }

    @Test
    fun testControllableSuspended() = testControllableSuspended {
        TestClass("foo", it) { initial, notifyOnChangedOnly, notifyForExisting, storePreviousValues, scope, mutex ->
            Controllable(initial, notifyOnChangedOnly, notifyForExisting, storePreviousValues, scope, mutex)
        }
    }

    @Test
    fun testControllable2Suspended() = testControllableSuspended {
        TestClass("foo", it) { initial, notifyOnChangedOnly, notifyForExisting, storePreviousValues, scope, mutex ->
            Controllable2<Any?, String>(
                initial,
                notifyOnChangedOnly,
                notifyForExisting,
                storePreviousValues,
                scope,
                mutex
            )
        }
    }

    fun <S : IControlledChangedScope2<Any?, String>, C : IControllableBase<Any?, String, S>>
            testControllableSuspended(
        inputFactory: (Map<String, ParamInstance<*>>) -> TestClass<S, C>
    ) = runTest(30.seconds) {
        parameterMix(
            inputFactory,
            param("storePreviousValues", true, false),
            param("notifyForExisting", true, false),
            param("notifyOnChangedOnly", true, false),
            param("scope", DefaultCoroutineScope(), null),
            param("mutex", Mutex(), null)
        ) {
//            println(this)
            if (scope != null) {
                val testClass = this

                testClass.controllable.controlS { /*Log.d("111=$it");*/ if (value == "foo") value = "boo" }
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

        data class TestClass<out S : IControlledChangedScope2<Any?, String>, out C : IControllableBase<Any?, String, S>>(
            val initial: String,
            val map: Map<String, ParamInstance<*>>,
            val factory: (
                initial: String,
                notifyOnChangedOnly: Boolean,
                notifyForExisting: Boolean,
                storePreviousValues: Boolean,
                scope: CoroutineScope?,
                mutex: Mutex?
            ) -> C
        ) {
            val storePreviousValues: Boolean by bind(map)
            val notifyForExisting: Boolean by bind(map)
            val notifyOnChangedOnly: Boolean by bind(map)
            val scope: CoroutineScope? by bind(map)
            val mutex: Mutex? by bind(map)

            val controllable = factory(
                initial, notifyOnChangedOnly, notifyForExisting,
                storePreviousValues, scope, mutex
            )
            var value by controllable
        }
    }
}