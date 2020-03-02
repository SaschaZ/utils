@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.MachineExTest.TestEvent.*
import de.gapps.utils.statemachine.MachineExTest.TestState.*
import de.gapps.utils.statemachine.scopes.on
import de.gapps.utils.statemachine.scopes.set
import org.junit.jupiter.api.Test


class MachineExTest {

    sealed class TestState : IState {
        override fun toString(): String = this::class.name

        object INITIAL : TestState()
        object A : TestState()
        object B : TestState()
        object C : TestState()
        object D : TestState()
    }

    sealed class TestEvent : IEvent {
        override fun toString(): String = this::class.name

        object FIRST : TestEvent()
        object SECOND : TestEvent()
        object THIRD : TestEvent()
        object FOURTH : TestEvent()
    }

    @Test
    fun testBuilder() = runTest {
        var executed = 0
        MachineEx<IData, TestEvent, TestState>(INITIAL) {
            on event FIRST withState INITIAL set A
            on event FIRST + SECOND + THIRD withState A + B set C
            on event THIRD withState C execAndSet { executed++; D }
            on event FOURTH withState D set B
            on state D exec { executed++ }
        }.run {
            state assert INITIAL

            set eventSync FIRST
            state assert A
            executed assert 0

            set eventSync SECOND
            state assert C
            executed assert 0

            set eventSync THIRD
            state assert D
            executed assert 1

            set eventSync FOURTH
            state assert B
            executed assert 2

            set eventSync FIRST
            state assert C
            executed assert 2
        }
    }

    @Test
    fun testBuilderSyncSet() = runTest {
        MachineEx<IData, TestEvent, TestState>(
            INITIAL
        ) {
            on event +FIRST withState +INITIAL set A
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
