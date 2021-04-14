@file:Suppress("unused", "ClassName")

package dev.zieger.utils.statemachine

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.isNull
import dev.zieger.utils.core_testing.assertion2.isTrue
import dev.zieger.utils.core_testing.assertion2.rem
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log2.LogScope
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.DEBUG
import dev.zieger.utils.statemachine.TestData.*
import dev.zieger.utils.statemachine.TestEvent.*
import dev.zieger.utils.statemachine.TestEvent.TEST_EVENT_GROUP.*
import dev.zieger.utils.statemachine.TestState.*
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_DEFG.*
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_DEFG.TEST_STATE_GROUP_FG.F
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_DEFG.TEST_STATE_GROUP_FG.G
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_HI.H
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_HI.I
import dev.zieger.utils.statemachine.conditionelements.AbsEvent
import dev.zieger.utils.statemachine.conditionelements.AbsState
import dev.zieger.utils.statemachine.conditionelements.X
import dev.zieger.utils.statemachine.conditionelements.combo
import dev.zieger.utils.time.duration.seconds
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.cancel


class MachineExTest : FunSpec({

    ENABLE_COLORED_LOG
    timeout = 10.seconds.millis

    val scope = DefaultCoroutineScope()

    beforeTest {
        scope.reset()
    }

    afterTest {
        scope.cancel()
    }

    test("Slave Bug") {
        MachineEx(INITIAL, scope, debugLevel = DEBUG) {
            +THIRD + INITIAL execAndSet {
                C
            }
            +THIRD * TestEventData("foo") + INITIAL execAndSet {
                eventData<TestEventData>().foo isEqual "foo" % "data test"
                D
            }
        }.run {
            state isEqual INITIAL

            fire eventSync THIRD * TestEventData("foo")
            state isEqual D
        }
    }

    test("complex") {
        var executed = 0
        var executed2 = 0
        MachineEx(INITIAL, scope, debugLevel = DEBUG) {
            +FIRST + INITIAL set A * TestStateData(true)
            +SECOND * TestEventData set C
            +FIFTH + A * TestStateData set E
            +FIRST + SECOND + THIRD + A + B + E set C
            +THIRD + C execAndSet {
                throw IllegalStateException("This state should never be active")
            }
            +THIRD * TestEventData("foo") + C execAndSet {
                executed++
                eventData<TestEventData>().foo isEqual "foo" % "data test"
                D
            }
            +FOURTH + D set B
            +C exec { executed++ }
            +C - FIRST exec { executed2++ }
        }.run {
            state isEqual INITIAL

            fire eventSync FIRST isEqual A * TestStateData(true)
            state isEqual A
            stateData isEqual TestStateData(true)
            executed isEqual 0 % "event FIRST with state INITIAL"
            executed2 isEqual 0 % "event FIRST with state INITIAL2"

            fire eventSync FIFTH
            state isEqual E
            executed isEqual 0 % "event FIFTH with state A"
            executed2 isEqual 0 % "event FIFTH with state A2"

            fire eventSync SECOND * TestEventData("moo")
            state isEqual C
            executed isEqual 1 % "event SECOND with state A"
            executed2 isEqual 1 % "event SECOND with state A#2"

            fire eventSync THIRD * TestEventData("foo")
            state isEqual D
            executed isEqual 2 % "event THIRD with state C"
            executed2 isEqual 1 % "event THIRD with state C#2"

            fire eventSync FOURTH
            state isEqual B
            executed isEqual 2 % "event FOURTH with state D"
            executed2 isEqual 1 % "event FOURTH with state D#2"

            fire eventSync FIRST
            state isEqual C % "FIRST with B"
            executed isEqual 3 % "event FIRST with state B"
            executed2 isEqual 1 % "event FIRST with state B#2"
        }
    }

    test("builder sync set") {
        MachineEx(INITIAL, scope, debugLevel = DEBUG) {
            +FIRST + INITIAL set A
        }.run {
            state isEqual INITIAL

            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire eventSync FIRST
            state isEqual A
        }
    }

    test("previous") {
        var lastEvent: AbsEvent? = null
        var lastState: AbsState? = null
        MachineEx(INITIAL, scope, debugLevel = DEBUG) {
            +FIRST + INITIAL[0] set A
            +SECOND + A + FIRST[1] + INITIAL[1] - C set !B * TestEventData("bäm")
            +THIRD + FIRST[X] + B + A[1] + INITIAL[2] set C * TestEventData
            +FOURTH + FIRST[3] + !C[0] + B[1] + A[2] + INITIAL[X] set D
            +FIFTH + D + C[1] + !B[2] + A[3] + INITIAL[3] set E
            +!TestEvent exec {
                lastEvent = this.eventCombo.master
                println("event: $lastEvent")
            }
            +!TestState exec {
                lastState = this.stateCombo.master
                println("state: $lastState")
            }
        }.run {
            state isEqual INITIAL

            fire eventSync FIRST
            state isEqual A
            lastEvent isEqual FIRST
            lastState isEqual A

            fire eventSync SECOND
            state isEqual B
            lastEvent isEqual SECOND
            lastState isEqual B

            fire eventSync THIRD
            state isEqual C
            lastEvent isEqual THIRD
            lastState isEqual C

            fire eventSync FOURTH
            state isEqual D

            fire eventSync FIFTH
            state isEqual D // because INITIAL[3] is false
        }
    }

    test("data") {
        MachineEx(INITIAL, scope, debugLevel = DEBUG) {
            +FIRST + INITIAL * TestEventData set A
            +SECOND + INITIAL set A * TestStateData(false)
            +THIRD + A[1] * TestStateData(false) set C
            +FOURTH + A * TestStateData set B
            +FIFTH * TestEventData("foo") + C set D
            +FOURTH * TestEventData("moo") + D set E
        }.run {
            state isEqual INITIAL
            stateData.isNull()

            fire eventSync FIRST isEqual INITIAL.combo
            state isEqual INITIAL
            stateData.isNull()

            fire eventSync SECOND isEqual A * TestStateData(false)
            state isEqual A
            stateData isEqual TestStateData(false)

            fire eventSync THIRD isEqual A * TestStateData(false)
            state isEqual A
            stateData isEqual TestStateData(false)

            fire eventSync FOURTH
            state isEqual B

            fire eventSync THIRD
            state isEqual C % "THIRD"

            fire eventSync FIFTH
            state isEqual C % "FIFTH without data"

            fire eventSync FIFTH * TestEventData2("foo")
            state isEqual C % "FIFTH with TestEventData2"

            fire eventSync FIFTH * TestEventData("foo")
            state isEqual D % "FIFTH with TestEventData"

            fire eventSync FOURTH * TestEventData("foo")
            state isEqual D % "FOURTH with TestEventData"

            fire eventSync FOURTH * TestEventData("moo")
            state isEqual E
        }
    }

    test("group") {
        MachineEx(INITIAL, scope, debugLevel = DEBUG) {
            +TestEvent + INITIAL set D
            +SECOND + TEST_STATE_GROUP_DEFG set E
            +THIRD * TestData + TEST_STATE_GROUP_FG set G
            +FOURTH + TEST_STATE_GROUP_DEFG set F
            +FIFTH + TEST_STATE_GROUP_HI set I
            +SIXTH set H
        }.run {
            state isEqual INITIAL % "INITIAL"

            fire eventSync FIRST
            state isEqual D % "FIRST"

            fire eventSync SECOND
            state isEqual E % "SECOND"

            fire eventSync THIRD * TestEventData2("bam")
            state isEqual E % "THIRD"

            fire eventSync FOURTH
            state isEqual F % "FOURTH"

            fire eventSync THIRD * TestEventData2("bom")
            state isEqual G % "THIRD#2"

            fire eventSync FIFTH
            state isEqual G % "FIFTH"

            fire eventSync SIXTH
            state isEqual H % "SIXTH"

            fire eventSync FIFTH
            state isEqual I % "FIFTH#2"
        }
    }

    test("subclass of") {
        val event = FOURTH
        val eventGroup = TEST_EVENT_GROUP

        Matcher(MatchScope(event.combo, A.combo), LogScope).apply {
            eventGroup.match().isTrue()
        }
    }

    test("external") {
        var isActive = false
        MachineEx(INITIAL, scope, debugLevel = DEBUG) {
            +FIRST + INITIAL + { isActive } set A * TestStateData(true)
            +SECOND + C + E - { isActive } set B
            +THIRD + A * TestStateData + B - D[X] + { isActive } set C
            +FOURTH + A[0..10] * TestStateData + B set D
            +SIXTH - { !isActive } set E
        }.run {
            state isEqual INITIAL % "Initial"

            fire eventSync FIRST
            state isEqual INITIAL % "FIRST and still INITIAL"

            isActive = true
            fire eventSync FIRST
            state isEqual A % "FIRST with isActive == true"

            fire eventSync THIRD
            state isEqual C % "THIRD with isActive == true"

            fire eventSync SECOND
            state isEqual C % "SECOND with isActive == true"

            isActive = false
            fire eventSync SECOND
            state isEqual B % "SECOND with isActive == false"

            fire eventSync FOURTH
            state isEqual D % "FOURTH with isActive == false"

            fire eventSync SIXTH
            state isEqual D % "SIXTH with isActive == false"

            isActive = true
            fire eventSync SIXTH
            state isEqual E % "SIXTH with isActive == true"
        }
    }
})
