@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.statemachine.MachineExTest.Event2.*
import de.gapps.utils.statemachine.MachineExTest.State2.*
import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.time.TimeEx
import org.junit.Test

import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MachineExTest {

    enum class State2 : IState by State() {
        INITIAL,
        A,
        B,
        C,
        D
    }

    enum class Event2 : IEvent by Event() {
        FIRST,
        SECOND,
        THIRD,
        FOURTH
    }

    infix fun <E: Any> E.or(o: E) = listOf(this, o)
    infix fun <E: Any> List<E>.or(o: E): List<E> = toMutableList().also { it.add(o) }

    @Test
    fun testIt() {
        var executed = false
        machineEx<Event2, State2>(
            INITIAL
        ) {
            on event FIRST with INITIAL changeTo A
            on eventOf (SECOND or FIRST or THIRD) withOneOf (A or B) changeTo C
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
        MachineEx<Event2, State2>(
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