package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.DEBUG
import dev.zieger.utils.statemachine.TestData.TestStateData
import dev.zieger.utils.statemachine.TestEvent.*
import dev.zieger.utils.statemachine.TestEvent.TEST_EVENT_GROUP.FOURTH
import dev.zieger.utils.statemachine.TestState.*
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_DEFG.D
import dev.zieger.utils.statemachine.conditionelements.X
import dev.zieger.utils.statemachine.conditionelements.combo
import dev.zieger.utils.time.seconds
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class MachineExFunctionDslTest : FunSpec({

    timeout = 10.seconds.millis.toLong()

    test("test function dsl") {
        MachineEx(INITIAL, DefaultCoroutineScope(), debugLevel = DEBUG) {
            onEvent(FIRST)
                .andState(INITIAL)
                .set(A)
            onEvent(SECOND)
                .andState(A)
                .set(B data TestStateData(true))
            onEvent(THIRD)
                .andState(B data TestStateData)
                .and { true }
                .set(C)
            onEvent(FOURTH)
                .andState(C, B previous X data TestStateData(true))
                .execAndSet { D }
        }.run {
            state shouldBe INITIAL

            fire eventSync FIRST shouldBe A.combo
            fire eventSync SECOND shouldBe (B data TestStateData(true))
            fire eventSync THIRD shouldBe C.combo
            fire eventSync FOURTH shouldBe D.combo
        }
    }
})