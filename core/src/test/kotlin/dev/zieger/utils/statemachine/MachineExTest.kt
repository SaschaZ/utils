@file:Suppress("unused", "ClassName")

package dev.zieger.utils.statemachine

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.DEBUG
import dev.zieger.utils.statemachine.TestData.*
import dev.zieger.utils.statemachine.TestEvent.*
import dev.zieger.utils.statemachine.TestEvent.TEST_EVENT_GROUP.*
import dev.zieger.utils.statemachine.TestState.*
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_DEFG.D
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_DEFG.E
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_DEFG.TEST_STATE_GROUP_FG.F
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_DEFG.TEST_STATE_GROUP_FG.G
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_HI.H
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_HI.I
import dev.zieger.utils.time.duration.seconds
import org.junit.jupiter.api.Test

class MachineExTest {

    @Test
    fun testComplex() = runTest(10.seconds) {
        var executed = 0
        var executed2 = 0
        MachineEx(TestState.INITIAL) {
            +TestEvent.FIRST + INITIAL set A * TestStateData(true)
            +SECOND * TestEventData set C
            +FIFTH + A * TestStateData set E
            +FIRST + SECOND + THIRD + A + B + E set C
            +THIRD + C execAndSet {
                throw IllegalStateException("This state should never be active")
            }
            +THIRD * TestEventData("foo") + C execAndSet {
                executed++; eventData<TestEventData>().foo assert "foo" % "data test"; D
            }
            +FOURTH + D set B
            +C exec { executed++ }
            +C - FIRST exec { executed2++ }
        }.run {
            state() assert INITIAL

            fire eventSync FIRST
            state() assert A
            executed assert 0 % "event FIRST with state INITIAL"
            executed2 assert 0 % "event FIRST with state INITIAL2"

            fire eventSync FIFTH
            state() assert E
            executed assert 0 % "event FIFTH with state A"
            executed2 assert 0 % "event FIFTH with state A2"

            fire eventSync SECOND * TestEventData("moo")
            state() assert C
            executed assert 1 % "event SECOND with state A"
            executed2 assert 1 % "event SECOND with state A#2"

            fire eventSync THIRD * TestEventData("foo")
            state() assert D
            executed assert 2 % "event THIRD with state C"
            executed2 assert 1 % "event THIRD with state C#2"

            fire eventSync FOURTH
            state() assert B
            executed assert 2 % "event FOURTH with state D"
            executed2 assert 1 % "event FOURTH with state D#2"

            fire eventSync FIRST
            state() assert C % "FIRST with B"
            executed assert 3 % "event FIRST with state B"
            executed2 assert 1 % "event FIRST with state B#2"
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
            +FIRST + INITIAL[0] set A
            +SECOND + A + INITIAL[1] set B
            +THIRD + B + A[1] + INITIAL[2] set C
            +FOURTH + C[0] + B[1] + A[2] + INITIAL[3] set D
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
            +FIRST + INITIAL * TestEventData set A
            +SECOND + INITIAL set A * TestStateData(false)
            +THIRD + A[1] * TestStateData(false) set C
            +FOURTH + A * TestStateData set B
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
            state() assert C % { "THIRD" }

            fire eventSync FIFTH
            state() assert C

            fire eventSync FIFTH * TestEventData2("foo")
            state() assert C % { "FIFTH with TestEventData2" }

            fire eventSync FIFTH * TestEventData("foo")
            state() assert D % { "FIFTH with TestEventData" }

            fire eventSync FOURTH * TestEventData("foo")
            state() assert D % "FOURTH with TestEventData"

            fire eventSync FOURTH * TestEventData("moo")
            state() assert E
        }
    }

    @Test
    fun testGroup() = runTest {
        MachineEx(INITIAL, debugLevel = DEBUG) {
            +TestEvent + INITIAL set D
            +SECOND + TEST_STATE_GROUP_DEFG set E
            +THIRD * TestData + TEST_STATE_GROUP_DEFG.TEST_STATE_GROUP_FG set G
            +FOURTH + TEST_STATE_GROUP_DEFG set F
            +FIFTH + TEST_STATE_GROUP_HI set I
            +SIXTH set H
        }.run {
            state() assert INITIAL % "INITIAL"

            fire eventSync FIRST
            state() assert D % "FIRST"

            fire eventSync SECOND
            state() assert E % "SECOND"

            fire eventSync THIRD * TestEventData2("bam")
            state() assert E % "THIRD"

            fire eventSync FOURTH
            state() assert F % "FOURTH"

            fire eventSync THIRD * TestEventData2("bom")
            state() assert G % "THIRD#2"

            fire eventSync FIFTH
            state() assert G % "FIFTH"

            fire eventSync SIXTH
            state() assert H % "SIXTH"

            fire eventSync FIFTH
            state() assert I % "FIFTH#2"
        }
    }

    @Test
    fun testSubclassOf() = runTest {
        val event = FOURTH
        val eventGroup = TEST_EVENT_GROUP

        eventGroup.match(event, emptyList()) assert true
        event.match(eventGroup, emptyList()) assert true
    }

    @Test
    fun testExternal() = runTest {
        var isActive = false
        MachineEx(INITIAL, debugLevel = DEBUG) {
            +FIRST + INITIAL + { isActive } set A * TestStateData(true)
            +SECOND + C + E - { isActive } set B
            +THIRD + A * TestStateData + B - D[X] + { isActive } set C
            +FOURTH + A[0..10] * TestStateData + B set D
            +SIXTH - { !isActive } set E
        }.run {
            state() assert INITIAL % "Initial"

            fire eventSync FIRST
            state() assert INITIAL % "FIRST and still INITIAL"

            isActive = true
            fire eventSync FIRST
            state() assert A % "FIRST with isActive == true"

            fire eventSync THIRD
            state() assert C % "THIRD with isActive == true"

            fire eventSync SECOND
            state() assert C % "SECOND with isActive == true"

            isActive = false
            fire eventSync SECOND
            state() assert B % "SECOND with isActive == false"

            fire eventSync FOURTH
            state() assert D % "FOURTH with isActive == false"

            fire eventSync SIXTH
            state() assert D % "SIXTH with isActive == false"

            isActive = true
            fire eventSync SIXTH
            state() assert E % "SIXTH with isActive == true"
        }
    }
}
