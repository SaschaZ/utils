@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.log.logV
import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.MachineExTest.State.*
import de.gapps.utils.statemachine.MachineExTest.TestEvent.*
import de.gapps.utils.statemachine.scopes.div
import de.gapps.utils.statemachine.scopes.lvl0.on
import de.gapps.utils.statemachine.scopes.lvl0.set
import de.gapps.utils.statemachine.scopes.lvl1.event
import de.gapps.utils.statemachine.scopes.lvl1.events
import de.gapps.utils.statemachine.scopes.lvl1.state
import de.gapps.utils.statemachine.scopes.lvl2.withState
import de.gapps.utils.statemachine.scopes.lvl2.withStates
import de.gapps.utils.statemachine.scopes.lvl3.changeStateTo
import de.gapps.utils.statemachine.scopes.lvl3.execute
import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.runTest
import io.kotlintest.specs.AnnotationSpec


class MachineExTest : AnnotationSpec() {

    sealed class State : IState {
        override fun toString(): String = this::class.name

        object INITIAL : State()
        object A : State()
        object B : State()
        object C : State()
        object D : State()
    }

    sealed class TestEvent : Event() {
        override fun toString(): String = this::class.name

        object FIRST : TestEvent()
        object SECOND : TestEvent()
        object THIRD : TestEvent()
        object FOURTH : TestEvent()
    }

    @Test
    fun testBuilder() = runTest {
        var executed = false
        machineEx<Event, State>(
            INITIAL
        ) {
            on event FIRST withState INITIAL changeStateTo A logV { m = "key=$it" }
            on events FIRST / SECOND / THIRD withStates A / B changeStateTo C
            on event THIRD withState C execute { D }
            on event FOURTH withState D changeStateTo B
            on state D runOnly { executed = true }
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
            executed assert false

            set eventSync SECOND
            state assert C
            executed assert false

            set eventSync THIRD
            state assert D
            executed assert false

            set eventSync FOURTH
            state assert B

            set eventSync FIRST
            state assert C
        }
    }

    @Test
    fun testConstructor() = runTest {
        MachineEx<Event, State>(
            INITIAL
        ) { e ->
            (e == FIRST && state == INITIAL).let {
                if (it) A else throw IllegalStateException("")
            }
        }.run {
            set eventSync FIRST
            state assert A
        }
    }
}
