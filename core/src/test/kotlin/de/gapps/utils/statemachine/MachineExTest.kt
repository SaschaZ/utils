@file:Suppress("unused", "ClassName")

package de.gapps.utils.statemachine

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.onFail
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.statemachine.ConditionElement.Master.Group.StateGroup
import de.gapps.utils.statemachine.ConditionElement.Master.Single.Event
import de.gapps.utils.statemachine.ConditionElement.Master.Single.State
import de.gapps.utils.statemachine.ConditionElement.Slave.Data
import de.gapps.utils.statemachine.MachineExTest.TestData.*
import de.gapps.utils.statemachine.MachineExTest.TestEvent.*
import de.gapps.utils.statemachine.MachineExTest.TestEvent.TEST_EVENT_GROUP.FIFTH
import de.gapps.utils.statemachine.MachineExTest.TestEvent.TEST_EVENT_GROUP.FOURTH
import de.gapps.utils.statemachine.MachineExTest.TestState.*
import de.gapps.utils.statemachine.MachineExTest.TestState.TEST_STATE_GROUP.D
import de.gapps.utils.statemachine.MachineExTest.TestState.TEST_STATE_GROUP.E
import de.gapps.utils.time.duration.seconds
import org.junit.jupiter.api.Test

class MachineExTest {

    sealed class TestState(ignoreSlave: Boolean = false) : State() {

        object INITIAL : TestState()
        object A : TestState()
        object B : TestState()
        object C : TestState()

        sealed class TEST_STATE_GROUP : TestState() {
            object D : TEST_STATE_GROUP()
            object E : TEST_STATE_GROUP()

            companion object : StateGroup<TEST_STATE_GROUP>(TEST_STATE_GROUP::class)
        }

        companion object : StateGroup<TestState>(TestState::class)
    }

    sealed class TestData : Data() {

        data class TestEventData(val foo: String) : TestData() {
            companion object : Type<TestEventData>(TestEventData::class)
        }

        data class TestEventData2(val foo: String) : TestData() {
            companion object : Type<TestEventData2>(TestEventData2::class)
        }

        data class TestStateData(val moo: Boolean) : TestData() {
            companion object : Type<TestStateData>(TestStateData::class)
        }

        companion object : Type<TestData>(TestData::class)
    }

    sealed class TestEvent(ignoreData: Boolean = false) : Event() {

        object FIRST : TestEvent()
        object SECOND : TestEvent()
        object THIRD : TestEvent()

        sealed class TEST_EVENT_GROUP : TestEvent() {
            object FOURTH : TestEvent()
            object FIFTH : TestEvent()
            object SIXTH : TestEvent()

            companion object : Group.EventGroup<TEST_EVENT_GROUP>(TEST_EVENT_GROUP::class)
        }

        companion object : Group.EventGroup<TestEvent>(TestEvent::class)
    }

    @Test
    fun testComplex() = runTest(10.seconds) {
        var executed = 0
        var executed2 = 0
        MachineEx(INITIAL) {
            +FIRST + INITIAL set A * TestStateData(true)
            +SECOND * TestData.TestEventData set C
            +FIFTH + A * TestData.TestStateData set E
            +FIRST + SECOND + THIRD + A + B + E set C
            +THIRD + C execAndSet {
                throw IllegalStateException("This state should never be active")
            }
            +THIRD * TestEventData("foo") + C execAndSet {
                executed++; eventData<TestEventData>().foo onFail "data test" assert "foo"; D
            }
            +FOURTH + D set B
            +C exec { executed++ }
            +C - FIRST exec { executed2++ }
        }.run {
            state() assert INITIAL

            fire eventSync FIRST
            state() assert A
            executed onFail "event FIRST with state INITIAL" assert 0
            executed2 onFail "event FIRST with state INITIAL2" assert 0

            fire eventSync FIFTH
            state() assert E
            executed onFail "event FIFTH with state A" assert 0
            executed2 onFail "event FIFTH with state A2" assert 0

            fire eventSync SECOND * TestEventData("moo")
            state() assert C
            executed onFail "event SECOND with state A" assert 1
            executed2 onFail "event SECOND with state A#2" assert 1

            fire eventSync THIRD * TestEventData("foo")
            state() assert D
            executed onFail "event THIRD with state C" assert 2
            executed2 onFail "event THIRD with state C#2" assert 1

            fire eventSync FOURTH
            state() assert B
            executed onFail "event FOURTH with state D" assert 2
            executed2 onFail "event FOURTH with state D#2" assert 1

            fire eventSync FIRST
            state() assert C
            executed onFail "event FIRST with state B" assert 3
            executed2 onFail "event FIRST with state B#2" assert 1
        }
    }

    @Test
    fun testBuilderSyncSet() = runTest {
        MachineEx(INITIAL) {
            +FIRST + INITIAL set A
        }.run {
            state() assert INITIAL

            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire eventSync FIRST
            state() assert A
        }
    }

    @Test
    fun testPrev() = runTest {
        MachineEx(INITIAL) {
            +FIRST + INITIAL set A
            +SECOND + A + INITIAL[1] set B
            +THIRD + B + A[1] + INITIAL[2] set C
            +FOURTH + C + B[1] + A[2] + INITIAL[3] set D
            +FIFTH + D + C[1] + B[2] + A[3] + INITIAL[3] set E
        }.run {
            state() assert INITIAL

            fire eventSync FIRST
            state() assert A

            fire eventSync SECOND
            state() assert B

            fire eventSync THIRD
            state() assert C

            fire eventSync FOURTH
            state() assert D

            fire eventSync FIFTH
            state() assert D // because INITIAL[3] is false
        }
    }

    @Test
    fun testData() = runTest {
        MachineEx(INITIAL) {
            +FIRST + INITIAL * TestData.TestEventData set A
            +SECOND + INITIAL set A * TestStateData(false)
            +THIRD + A[1] * TestStateData(false) set C
            +FOURTH + A * TestData.TestStateData set B
            +FIFTH * TestEventData("foo") + C set D
            +FOURTH * TestEventData("moo") + D set E
        }.run {
            state() assert INITIAL

            fire eventSync FIRST
            state() assert INITIAL

            fire eventSync SECOND
            state() assert A

            fire eventSync THIRD
            state() assert A

            fire eventSync FOURTH
            state() assert B

            fire eventSync THIRD
            state() onFail { "THIRD" } assert C

            fire eventSync FIFTH
            state() assert C

            fire eventSync FIFTH * TestEventData2("foo")
            state() onFail { "FIFTH with TestEventData2" } assert C

            fire eventSync FIFTH * TestEventData("foo")
            state() onFail { "FIFTH with TestEventData" } assert D

            fire eventSync FOURTH * TestEventData("foo")
            state() onFail "FOURTH with TestEventData" assert D

            fire eventSync FOURTH * TestEventData("moo")
            state() assert E
        }
    }

    @Test
    fun testGroup() = runTest {
        MachineEx(INITIAL) {
            +TEST_EVENT_GROUP + INITIAL set D
            +FIFTH + TEST_STATE_GROUP set C
            +FIRST * TestData + C set A
        }.run {
            state() assert INITIAL

            fire eventSync FOURTH
            state() assert D

            fire eventSync FIFTH
            state() assert C

            fire eventSync FIRST * TestEventData2("bam")
            state() assert A
        }
    }
}
