@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.MachineExTest.Event.*
import de.gapps.utils.statemachine.MachineExTest.State.*
import de.gapps.utils.testing.assertion.assert
import org.junit.Test

import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MachineExTest {

    enum class State : IState {
        INITIAL,
        A,
        B,
        C,
        D
    }

    enum class Event : IEvent {
        FIRST,
        SECOND,
        THIRD,
        FOURTH
    }

    @Test
    fun testIt() {
        var executed = false
        machineEx<Event, State>(
            INITIAL
        ) {
            on event FIRST with INITIAL changeTo A
            on eventOf listOf(SECOND, FIRST) withOneOf listOf(A, B) changeTo C
            on event THIRD with C run { D }
            on event FOURTH with D changeTo B
            on state D run { executed = true }
        }.run {
            INITIAL assert state

            set event FIRST
            state assert A
            assertFalse(executed)

            set event SECOND
            C assert state
            assertFalse(executed)

            set event THIRD
            D assert state
            assertTrue(executed)

            set event FOURTH
            B assert state

            set event FIRST
            C assert state
        }
    }

    @Test
    fun testCtor() {
        MachineEx<Event, State>(
            INITIAL
        ) { e ->
            when (e to state) {
                FIRST to INITIAL -> {
                    A
                }
                else -> throw IllegalStateException("")
            }
        }.run {
            set event FIRST
            A assert state
        }
    }
}