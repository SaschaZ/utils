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
            on event FIRST andState INITIAL set A
            on event FIRST * SECOND * THIRD andState A * B set C
            on event THIRD + TestEventData("foo") andState C execAndSet {
                executed++; eventData<TestEventData>()?.foo onFail "data test" assert "foo"; D
            }
            on event FOURTH andState D set B
            on state C exec { executed++ }
        }.run {
            state assert INITIAL

            set eventSync FIRST
            state assert A
            executed onFail "event FIRST with state INITIAL" assert 0

            set eventSync SECOND
            state assert C
//            executed onFail "event SECOND with state A" assert 1

            set eventSync THIRD
            state assert D
            executed onFail "event THIRD with state C" assert 2

            set eventSync FOURTH
            state assert B
//            executed onFail "event FOURTH with state D" assert 2

            set eventSync FIRST
            state assert C
//            executed onFail "event FIRST with state B" assert 3
        }
    }

    @Test
    fun testBuilderSyncSet() = runTest {
        MachineEx(INITIAL) {
            on event FIRST andState INITIAL set A
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
