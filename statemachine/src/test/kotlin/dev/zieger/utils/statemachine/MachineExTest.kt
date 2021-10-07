@file:Suppress("unused", "ClassName")

package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.log.LogScope
import dev.zieger.utils.log.filter.LogLevel
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
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.time.seconds
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancel


class MachineExTest : FunSpec({

    timeout = 10.seconds.millis.toLong()

    val scope = DefaultCoroutineScope()

    beforeTest {
        Log.logLevel = LogLevel.INFO
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
                eventData<TestEventData>().foo shouldBe "foo"
                D
            }
        }.run {
            state shouldBe INITIAL

            fire eventSync THIRD * TestEventData("foo")
            state shouldBe D
        }
    }

    test("complex") {
        var executed = 0
        var executed2 = 0
        MachineEx(INITIAL, scope, debugLevel = DEBUG) {
            +!Event exec {
                Log.i("incoming event $event")
            }
            +!State exec {
                Log.i("new State $state")
            }
            +FIRST + INITIAL set A * TestStateData(true)
            +SECOND * TestEventData set C
            +FIFTH + A * TestStateData set E
            +FIRST + SECOND + THIRD + A + B + E set C
            +THIRD + C execAndSet {
                throw IllegalStateException("This state should never be active")
            }
            +THIRD * TestEventData("foo") + C execAndSet {
                executed++
                eventData<TestEventData>().foo shouldBe "foo"
                D
            }
            +FOURTH + D set B
            +C exec { executed++ }
            +C - FIRST exec { executed2++ }
        }.run {
            state shouldBe INITIAL

            fire eventSync FIRST shouldBe A * TestStateData(true)
            state shouldBe A
            stateData shouldBe TestStateData(true)
            executed shouldBe 0
            executed2 shouldBe 0

            fire eventSync FIFTH
            state shouldBe E
            executed shouldBe 0
            executed2 shouldBe 0

            fire eventSync SECOND * TestEventData("moo")
            state shouldBe C
            executed shouldBe 1
            executed2 shouldBe 1

            fire eventSync THIRD * TestEventData("foo")
            state shouldBe D
            executed shouldBe 2
            executed2 shouldBe 1

            fire eventSync FOURTH
            state shouldBe B
            executed shouldBe 2
            executed2 shouldBe 1

            fire eventSync FIRST
            state shouldBe C
            executed shouldBe 3
            executed2 shouldBe 1
        }
    }

    test("builder sync set") {
        MachineEx(INITIAL, scope, debugLevel = DEBUG) {
            +FIRST + INITIAL set A
        }.run {
            state shouldBe INITIAL

            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire event THIRD
            fire event FOURTH
            fire eventSync FIRST
            state shouldBe A
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
            state shouldBe INITIAL

            fire eventSync FIRST
            state shouldBe A
            lastEvent shouldBe FIRST
            lastState shouldBe A

            fire eventSync SECOND
            state shouldBe B
            lastEvent shouldBe SECOND
            lastState shouldBe B

            fire eventSync THIRD
            state shouldBe C
            lastEvent shouldBe THIRD
            lastState shouldBe C

            fire eventSync FOURTH
            state shouldBe D

            fire eventSync FIFTH
            state shouldBe D // because INITIAL[3] is false
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
            state shouldBe INITIAL
            stateData shouldBe null

            fire eventSync FIRST shouldBe INITIAL.combo
            state shouldBe INITIAL
            stateData shouldBe null

            fire eventSync SECOND shouldBe A * TestStateData(false)
            state shouldBe A
            stateData shouldBe TestStateData(false)

            fire eventSync THIRD shouldBe A * TestStateData(false)
            state shouldBe A
            stateData shouldBe TestStateData(false)

            fire eventSync FOURTH
            state shouldBe B

            fire eventSync THIRD
            state shouldBe C

            fire eventSync FIFTH
            state shouldBe C

            fire eventSync FIFTH * TestEventData2("foo")
            state shouldBe C

            fire eventSync FIFTH * TestEventData("foo")
            state shouldBe D

            fire eventSync FOURTH * TestEventData("foo")
            state shouldBe D

            fire eventSync FOURTH * TestEventData("moo")
            state shouldBe E
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
            state shouldBe INITIAL

            fire eventSync FIRST
            state shouldBe D

            fire eventSync SECOND
            state shouldBe E

            fire eventSync THIRD * TestEventData2("bam")
            state shouldBe E

            fire eventSync FOURTH
            state shouldBe F

            fire eventSync THIRD * TestEventData2("bom")
            state shouldBe G

            fire eventSync FIFTH
            state shouldBe G

            fire eventSync SIXTH
            state shouldBe H

            fire eventSync FIFTH
            state shouldBe I
        }
    }

    test("subclass of") {
        val event = FOURTH
        val eventGroup = TEST_EVENT_GROUP

        Matcher(MatchScope(event.combo, A.combo), LogScope).apply {
            eventGroup.match() shouldBe true
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
            state shouldBe INITIAL

            fire eventSync FIRST
            state shouldBe INITIAL

            isActive = true
            fire eventSync FIRST
            state shouldBe A

            fire eventSync THIRD
            state shouldBe C

            fire eventSync SECOND
            state shouldBe C

            isActive = false
            fire eventSync SECOND
            state shouldBe B

            fire eventSync FOURTH
            state shouldBe D

            fire eventSync SIXTH
            state shouldBe D

            isActive = true
            fire eventSync SIXTH
            state shouldBe E
        }
    }
})
