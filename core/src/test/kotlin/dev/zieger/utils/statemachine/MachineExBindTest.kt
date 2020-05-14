package dev.zieger.utils.statemachine

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.statemachine.ConditionElement.Slave.Any
import dev.zieger.utils.statemachine.TestChildData.Data1
import dev.zieger.utils.statemachine.TestChildData.Data2
import dev.zieger.utils.statemachine.TestChildEvent.*
import dev.zieger.utils.statemachine.TestChildState.*
import dev.zieger.utils.statemachine.TestEvent.FIRST
import dev.zieger.utils.statemachine.TestEvent.THIRD
import dev.zieger.utils.statemachine.TestState.A
import dev.zieger.utils.statemachine.TestState.INITIAL
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class MachineExBindTest {

    @Test
    fun testBinding() = runBlocking {
        val child = MachineEx(CINITIAL) {
            +CFIRST + CINITIAL set CA
            +CSECOND + CA execAndSet { println("fooboo"); CB * Data1() }
            +CTHIRD + CB * Data1 set CC
            +CFIRST + CC set CD
            +CFIRST + CD set CINITIAL * Data2()
        }
        val host = MachineEx(INITIAL) {
            +FIRST + INITIAL set CINITIAL
            +TestChildEvent * Any + TestChildState * Any bind child
            +THIRD + CINITIAL * Data2 set A
        }.scope {
            child.state() assert CINITIAL
            state() assert INITIAL

            fire eventSync FIRST
            child.state() assert CINITIAL
            state() assert CINITIAL

            fire eventSync CFIRST
            child.state() assert CA
            state() assert CA

            fire eventSync CSECOND
            child.state assert CB * Data1()
            state assert CB * Data1()

            fire eventSync CTHIRD
            child.state() assert CC
            state() assert CC

            fire eventSync CFIRST
            child.state() assert CD
            state() assert CD

            fire eventSync CFIRST
            child.state assert CINITIAL * Data2()
            state assert CINITIAL * Data2()

            fire eventSync THIRD
            child.state assert CINITIAL * Data2()
            state() assert A
        }
    }
}