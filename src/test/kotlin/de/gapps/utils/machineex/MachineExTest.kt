@file:Suppress("unused")

package de.gapps.utils.machineex

import de.gapps.utils.machineex.MachineExTest.Event.*
import de.gapps.utils.machineex.MachineExTest.State.*
import io.kotlintest.specs.AnnotationSpec
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MachineExTest : AnnotationSpec() {

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
        machineEx<Event, State>(INITIAL) {
            on event FIRST with INITIAL changeTo A
            on eventOf listOf(SECOND, FIRST) withOneOf listOf(A, B) changeTo C
            on event THIRD with C run { D }
            on event FOURTH with D changeTo B
            on state D run { executed = true }
        }.run {
            INITIAL equals state

            set event FIRST
            A equals state
            assertFalse(executed)

            set event SECOND
            C equals state
            assertFalse(executed)

            set event THIRD
            D equals state
            assertTrue(executed)

            set event FOURTH
            B equals state

            set event FIRST
            C equals state
        }
    }

    @Test
    fun testCtor() {
        MachineEx<Event, State>(INITIAL) { e ->
            when (e to state) {
                FIRST to INITIAL -> {
                    A
                }
                else -> throw IllegalStateException("")
            }
        }.run {
            set event FIRST
            A equals state
        }
    }
}