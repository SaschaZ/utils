@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.MachineExTest.TestEvent.*
import de.gapps.utils.statemachine.MachineExTest.TestState.*
import de.gapps.utils.statemachine.scopes.MachineExScope
import de.gapps.utils.statemachine.scopes.definition.lvl0.on
import de.gapps.utils.statemachine.scopes.manipulation.set
import de.gapps.utils.statemachine.scopes.plus
import de.gapps.utils.statemachine.scopes.unaryPlus
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
        override fun toString(): String = "${this::class.name}(${this::class.name})"

        object FIRST : TestEvent()
        object SECOND : TestEvent()
        object THIRD : TestEvent()
        object FOURTH : TestEvent()
    }

    @Test
    fun testBuilder() = runTest {
        var executed = false
        machineEx<TestEvent, TestState>(INITIAL) {
            //            on eventTypes +FIRST withStates +INITIAL setState A
            on event +FIRST withState +INITIAL setState A
            on event +FIRST + SECOND + THIRD withState +A + B setState C
            on event +THIRD withState +C executeAndSetStateS { D }
            on event +FOURTH withState +D setState B
            on state +D execute { executed = true }
        }.run {
            state assert INITIAL

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
        MachineEx<TestEvent, TestState>(
            INITIAL,
            MachineExScope(DefaultCoroutineScope())
        ) { e ->
            (e == FIRST && state == INITIAL).let {
                if (it) A else throw IllegalStateException("")
            }
        }.run {
            set eventSync FIRST
            state assert A
        }
    }

    @Test
    fun testBuilderSyncSet() = runTest {
        machineEx<TestEvent, TestState>(
            INITIAL
        ) {
            on event +FIRST withState +INITIAL setState A
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
