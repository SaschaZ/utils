@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.onFail
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.statemachine.MachineExTest.TestEvent.*
import de.gapps.utils.statemachine.MachineExTest.TestState.*
import de.gapps.utils.statemachine.scopes.set
import de.gapps.utils.time.duration.minutes
import org.junit.jupiter.api.Test


class MachineExTest {

    sealed class TestState : State() {

        object INITIAL : TestState()
        object A : TestState()
        object B : TestState()
        object C : TestState()
        object D : TestState()
    }

    data class TestEventData(val foo: String)
    data class TestStateData(val moo: Boolean)

    sealed class TestEvent : Event() {

        object FIRST : TestEvent()
        object SECOND : TestEvent()
        object THIRD : TestEvent()
        object FOURTH : TestEvent()
    }

    @Test
    fun testBuilder() = runTest(10.minutes) {
        var executed = 0
        MachineEx(INITIAL) {
            - FIRST / INITIAL += A
            - FIRST * SECOND * THIRD / A * B += C
            - THIRD / C *= {
                executed++; eventData<TestEventData>()?.foo onFail "data test" assert "foo"; D
            }
            - FOURTH / D += B
            - C -= { executed++ }
        }.run {
            state.value assert INITIAL

            set eventSync FIRST
            state.value assert A
            executed onFail "event FIRST with state INITIAL" assert 0

            set eventSync SECOND
            state.value assert C
            executed onFail "event SECOND with state A" assert 1

            set eventSync THIRD + TestEventData("foo")
            state.value assert D
            executed onFail "event THIRD with state C" assert 2

            set eventSync FOURTH
            state.value assert B
            executed onFail "event FOURTH with state D" assert 2

            set eventSync FIRST
            state.value assert C
            executed onFail "event FIRST with state B" assert 3
        }
    }

    @Test
    fun testBuilderSyncSet() = runTest {
        MachineEx(INITIAL) {
            - FIRST / INITIAL += A
        }.run {
            state.value assert INITIAL

            set event FOURTH
            set event THIRD
            set event FOURTH
            set event THIRD
            set event FOURTH
            set event THIRD
            set event FOURTH
            set eventSync FIRST
            state.value assert A
        }
    }
}
