package dev.zieger.utils.statemachine

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.console.termColored
import dev.zieger.utils.log2.ColoredLogOutput
import dev.zieger.utils.log2.Log
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.DEBUG
import dev.zieger.utils.statemachine.TestData.TestStateData
import dev.zieger.utils.statemachine.TestEvent.*
import dev.zieger.utils.statemachine.TestEvent.TEST_EVENT_GROUP.FOURTH
import dev.zieger.utils.statemachine.TestState.*
import dev.zieger.utils.statemachine.TestState.TEST_STATE_GROUP_DEFG.D
import dev.zieger.utils.statemachine.conditionelements.combo
import dev.zieger.utils.time.duration.seconds
import io.kotest.core.TestConfiguration
import io.kotest.core.spec.style.FunSpec

val TestConfiguration.ENABLE_COLORED_LOG
    get() = beforeTest {
        Log.output = ColoredLogOutput()
        termColored { println(cyan("colored log initialized")) }
    }

class MachineExFunctionDslTest : FunSpec({

    ENABLE_COLORED_LOG
    timeout = 10.seconds.millis

    test("test function dsl") {
        MachineEx(INITIAL, DefaultCoroutineScope(), debugLevel = DEBUG) {
            onEvent(FIRST).withState(INITIAL).set(A)
            onEvent(SECOND).withState(A).set(B link TestStateData(true))
            onEvent(THIRD).withState(B link TestStateData).set(C)
            onEvent(FOURTH).withState(C, B.previous(1) link TestStateData(true)).set(D)

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
            fire eventSync SECOND isEqual B * TestStateData(true)
            fire eventSync THIRD isEqual C.combo
            fire eventSync FOURTH isEqual D.combo
        }
    }
})