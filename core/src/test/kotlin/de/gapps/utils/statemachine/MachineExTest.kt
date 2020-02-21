@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.log.logV
import de.gapps.utils.statemachine.MachineExTest.State.*
import de.gapps.utils.statemachine.MachineExTest.TestEvent.*
import de.gapps.utils.statemachine.scopes.div
import de.gapps.utils.statemachine.scopes.plus
import de.gapps.utils.statemachine.scopes.times
import de.gapps.utils.statemachine.scopes.withParameter
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

    sealed class TestEvent : Event<String, String>() {
        object FIRST : TestEvent() { init {
            put("foo", "test it")
        }
        }

        object SECOND : TestEvent()
        object THIRD : TestEvent()
        object FOURTH : TestEvent()
    }

    @Test
    fun testIt() {
        var executed = false
        machineEx<String, String, Event<String, String>, State>(
            INITIAL
        ) {
            on event FIRST withState INITIAL changeTo A logV { m = "key=$it" }
            on events FIRST / SECOND / THIRD withStates A / B changeTo C
            on event THIRD withState C run { D }
            on event FOURTH withState D changeTo B
            on state D runOnly { executed = true }
        }.run {
            state assert INITIAL

            set event FIRST withParameter "foo" * "boo" + "moo" * "woo"
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
        MachineEx<String, String, Event<String, String>, State>(
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
