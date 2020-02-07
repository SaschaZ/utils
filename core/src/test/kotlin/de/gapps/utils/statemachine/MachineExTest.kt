@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.MachineExTest.Event2.*
import de.gapps.utils.statemachine.MachineExTest.State2.*
import de.gapps.utils.testing.assertion.assert
import org.junit.Test

import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MachineExTest {

    enum class State2 : IState {
        INITIAL,
        A,
        B,
        C,
        D
    }

    sealed class Event2 : IEvent {
        class FIRST : Event2()
        class SECOND : Event2()
        class THIRD : Event2()
        class FOURTH : Event2()
    }

    @Test
    fun testIt() {
        var executed = false
        machineEx<Event2, State2>(
            INITIAL
        ) {
            on event FIRST() withState INITIAL changeTo A
            on events (SECOND() or FIRST() or THIRD()) withStates (A or B) changeTo C
            on event THIRD() withState C run { D }
            on event FOURTH() withState D changeTo B
            on state D onlyRun { executed = true }
        }.run {
            INITIAL assert state

            set event FIRST()
            state assert A
            assertFalse(executed)

            set event SECOND()
            C assert state
            assertFalse(executed)

            set event THIRD()
            D assert state
            assertTrue(executed)

            set event FOURTH()
            B assert state

            set event FIRST()
            C assert state
        }
    }

    @Test
    fun testCtor() {
        MachineEx<Event2, State2>(
            INITIAL
        ) { e ->
            when (e to state) {
                FIRST() to INITIAL -> {
                    A
                }
                else -> throw IllegalStateException("")
            }
        }.run {
            set event FIRST()
            A assert state
        }
    }
}