@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.onFail
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.MachineExTest.TestEvent.*
import de.gapps.utils.statemachine.MachineExTest.TestState.*
import de.gapps.utils.statemachine.scopes.on
import de.gapps.utils.statemachine.scopes.plus
import de.gapps.utils.statemachine.scopes.set
import org.junit.jupiter.api.Test


class MachineExTest {

    sealed class TestState : StateImpl<Any>() {
        override fun toString(): String = this::class.name

        object INITIAL : TestState()
        object A : TestState()
        object B : TestState()
        object C : TestState()
        object D : TestState()
    }

    data class TestEventData(val foo: String)
    data class TestStateData(val moo: Boolean)

    sealed class TestEvent : EventImpl<Any>() {

        override fun toString(): String = this::class.name

        object FIRST : TestEvent()
        object SECOND : TestEvent()
        object THIRD : TestEvent()
        object FOURTH : TestEvent()
    }

    @Test
    fun testBuilder() = runTest {
        var executed = 0
        MachineEx(INITIAL) {
            on event FIRST andState INITIAL += A + TestStateData(true)
            on event FIRST * SECOND * THIRD + TestEventData("boo") andState A * B + TestStateData(true) += C
            on event THIRD andState C += {
                executed++; eventData<TestEventData>()?.foo onFail "data test" assert "foo"; D
            }
            on event FOURTH andState D += B
            on state C -= { executed++ }
        }.run {
            state assert INITIAL

            set eventSync FIRST + TestEventData("woo")
            state assert A
            executed assert 0

            set eventSync SECOND + TestEventData("moo")
            state assert C
            executed assert 1

            set eventSync THIRD + TestEventData("foo")
            state assert D
            executed onFail "first" assert 2

            set eventSync FOURTH
            state assert B
            executed onFail "second" assert 2

            set eventSync FIRST
            state assert C
            executed onFail "third" assert 3
        }
    }

    @Test
    fun testBuilderSyncSet() = runTest {
        MachineEx(INITIAL) {
            on event FIRST andState INITIAL += A
        }.run {
            state assert INITIAL

            set event FOURTH
            set event THIRD
            set event FOURTH
            set event THIRD
            set event FOURTH
            set event THIRD
            set event FOURTH
            set eventSync FIRST
            state assert A
        }
    }
}
