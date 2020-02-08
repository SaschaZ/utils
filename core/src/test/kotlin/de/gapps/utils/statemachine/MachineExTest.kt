@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.MachineExTest.Event.*
import de.gapps.utils.statemachine.MachineExTest.State.*
import de.gapps.utils.testing.assertion.assert
import org.junit.Test

class MachineExTest {

    sealed class State : IState {
        object INITIAL : State()
        object A : State()
        object B : State()
        object C : State()
        object D : State()
    }

    sealed class Event : IEvent {
        object FIRST : Event()
        object SECOND : Event()
        object THIRD : Event()
        object FOURTH : Event()
    }

    @Test
    fun testIt() {
        var executed = false
        machineEx<Event, State>(
            INITIAL
        ) {
            on event FIRST withState INITIAL changeTo A
            on events (FIRST or SECOND or THIRD) withStates (A or B) changeTo C
            on event THIRD withState C run { D }
            on event FOURTH withState D changeTo B
            on state D runOnly { executed = true }
        }.run {
            state assert INITIAL

            set event FIRST
            state assert A
            executed assert false

            set event SECOND
            state assert C
            executed assert false

            set event THIRD
            state assert D
            executed assert false

            set event FOURTH
            state assert B

            set event FIRST
            state assert C
        }
    }

    @Test
    fun testCtor() {
        MachineEx<Event, State>(
            INITIAL
        ) { e ->
            (e::class.isInstance(FIRST)
                    && state::class.isInstance(INITIAL)).let {
                if (it) A else throw IllegalStateException("")
            }
        }.run {
            set event FIRST
            A assert state
        }
    }
}