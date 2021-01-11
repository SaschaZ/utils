package dev.zieger.utils.statemachine

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.DEBUG
import dev.zieger.utils.statemachine.MachineExTest.TestData.TestStateData
import dev.zieger.utils.statemachine.MachineExTest.TestEvent.*
import dev.zieger.utils.statemachine.MachineExTest.TestEvent.TEST_EVENT_GROUP.FOURTH
import dev.zieger.utils.statemachine.MachineExTest.TestState.*
import dev.zieger.utils.statemachine.MachineExTest.TestState.TEST_STATE_GROUP_DEFG.D
import dev.zieger.utils.statemachine.conditionelements.combo
import io.kotest.core.spec.style.FunSpec

class MachineExFunctionDslTest : FunSpec({

    test("test function dsl") {
        MachineEx(INITIAL, DefaultCoroutineScope(), debugLevel = DEBUG) {
            onEvent(FIRST).withState(INITIAL).set(A)
            onEvent(SECOND).withState(A).set(B(TestStateData(true)))
            onEvent(THIRD).withState(B(TestStateData)).set(C)
//            onEvent(FOURTH).withState(C, B).set(D)
//            +FOURTH + C + B(TestStateData)[1] set D

//            onEvent(TestEvent.ignoreSlave).exec {
//
//            }
//
//            onState(TestState.ignoreSlave).withEvent(SECOND).exec {
//
//            }
        }.run {
            state isEqual INITIAL

            fire eventSync FIRST isEqual A.combo
            fire eventSync SECOND isEqual B(TestStateData(true))
            fire eventSync THIRD isEqual C.combo
            fire eventSync FOURTH isEqual D.combo
        }
    }
})