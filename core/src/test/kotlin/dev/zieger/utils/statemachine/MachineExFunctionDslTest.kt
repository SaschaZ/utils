package dev.zieger.utils.statemachine

import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.statemachine.MachineExTest.TestData.TestStateData
import dev.zieger.utils.statemachine.MachineExTest.TestEvent.*
import dev.zieger.utils.statemachine.MachineExTest.TestEvent.TEST_EVENT_GROUP.FOURTH
import dev.zieger.utils.statemachine.MachineExTest.TestState.*
import dev.zieger.utils.statemachine.MachineExTest.TestState.TEST_STATE_GROUP_DEFG.D
import org.junit.jupiter.api.Test

class MachineExFunctionDslTest {

    @Test
    fun testFunctionDsl() = runTest {
        val machine = MachineEx(INITIAL, DefaultCoroutineScope()) {
            onEvent(FIRST).withState(INITIAL).set(A)
            onEvent(SECOND).withState(A).set(B(TestStateData(true)))
            onEvent(THIRD).withState(B(TestStateData)).set(C)
            onEvent(FOURTH).withState(C).withPrevious(B.ignoreSlave[1]).set(D)

            onEvent(MachineExTest.TestEvent.ignoreSlave).exec {

            }

            onState(MachineExTest.TestState.ignoreSlave).exec {

            }
        }.run {

        }
    }
}