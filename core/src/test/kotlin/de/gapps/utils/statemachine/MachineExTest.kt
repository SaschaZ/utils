@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.onFail
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.statemachine.BaseType.Data
import de.gapps.utils.statemachine.BaseType.Primary.Event
import de.gapps.utils.statemachine.BaseType.Primary.State
import de.gapps.utils.statemachine.MachineExTest.TestEvent.*
import de.gapps.utils.statemachine.MachineExTest.TestState.*
import de.gapps.utils.time.duration.seconds
import org.junit.jupiter.api.Test

class MachineExTest {

    sealed class TestState : State() {

        object INITIAL : TestState()
        object A : TestState()
        object B : TestState()
        object C : TestState()
        object D : TestState()
        object E : TestState()
    }

    data class TestEventData(val foo: String) : Data()
    data class TestStateData(val moo: Boolean) : Data()
    data class StickyTestData(val testVal: Boolean = true) : Data()

    sealed class TestEvent(ignoreData: Boolean = false) : Event(ignoreData) {

        object FIRST : TestEvent()
        object SECOND : TestEvent(true)
        object THIRD : TestEvent()
        object FOURTH : TestEvent()
        object FIFTH : TestEvent()
    }

    @Test
    fun testBuilder() = runTest(10.seconds) {
        var executed = 0
        var executed2 = 0
        MachineEx(INITIAL) {
            -FIRST + INITIAL set A
            -FIFTH + INITIAL[1] set E
            -FIRST + SECOND * TestEventData("moo") + THIRD + A + B + E set C
            -THIRD + C execAndSet {
                throw IllegalStateException("This state should never be active")
            }
            -THIRD * TestEventData("foo") + C execAndSet {
                executed++; eventData<TestEventData>().foo onFail "data test" assert "foo"; D
            }
            -FOURTH + D + C[1] set B
            -C exec { executed++ }
            -C - FIRST exec { executed2++ }
        }.run {
            state.value assert INITIAL

            fire eventSync FIRST
            state.value assert A
            executed onFail "event FIRST with state INITIAL" assert 0
            executed2 onFail "event FIRST with state INITIAL" assert 0

            fire eventSync FIFTH
            state.value assert E
            executed onFail "event FIFTH with state A" assert 0
            executed2 onFail "event FIFTH with state A" assert 0

            fire eventSync SECOND * TestEventData("moo")
            state.value assert C
            executed onFail "event SECOND with state A" assert 1
            executed2 onFail "event SECOND with state A#2" assert 1

            fire eventSync THIRD * TestEventData("foo")
            state.value assert D
            executed onFail "event THIRD with state C" assert 2
            executed2 onFail "event THIRD with state C#2" assert 1

            fire eventSync FOURTH
            state.value assert B
            executed onFail "event FOURTH with state D" assert 2
            executed2 onFail "event FOURTH with state D#2" assert 1

            fire eventSync FIRST
            state.value assert C
            executed onFail "event FIRST with state B" assert 3
            executed2 onFail "event FIRST with state B#2" assert 1
        }
    }

    @Test
    fun testBuilderSyncSet() = runTest {
        MachineEx(INITIAL) {
            -FIRST + INITIAL set A
        }.run {
            state.value assert INITIAL

            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire eventSync FIRST
            state.value assert A
        }
    }

    @Test
    fun testPrev() = runTest {
        MachineEx(INITIAL) {
            -FIRST + INITIAL set A
            -SECOND + A + INITIAL[1] set B
            -THIRD + B + A[1] + INITIAL[2] set C
            -FOURTH + C + B[1] + A[2] + INITIAL[3] set D
            -FIFTH + D + C[1] + B[2] + A[3] + INITIAL[3] set E
        }.run {
            state.value assert INITIAL

            fire eventSync FIRST
            state.value assert A

            fire eventSync SECOND
            state.value assert B

            fire eventSync THIRD
            state.value assert C

            fire eventSync FOURTH
            state.value assert D

            fire eventSync FIFTH
            state.value assert D // because INITIAL[3] is false
        }
    }
}
