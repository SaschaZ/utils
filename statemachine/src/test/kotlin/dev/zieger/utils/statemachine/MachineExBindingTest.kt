package dev.zieger.utils.statemachine

import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.statemachine.Events.*
import dev.zieger.utils.statemachine.Events.THIRD.*
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.DEBUG
import dev.zieger.utils.statemachine.States.*
import dev.zieger.utils.statemachine.States.C.*
import dev.zieger.utils.statemachine.TestData.Data0
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.dsl.times
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope

class TestContext {

    val childMachine: MachineEx
    val machine: MachineEx

    var lastChildState: C? = null
    var lastState: States? = null

    val scope: CoroutineScope

    init {
        lastChildState = null
        scope = DefaultCoroutineScope()
        childMachine = MachineEx(CA, scope, debugLevel = DEBUG) {
            +THIRD0 set CB
            +THIRD1 + CB set CC * Data0()
            +THIRD2 + CC * Data0 set CA
            +!C exec {
                lastChildState = this.state as C
            }
        }
        lastState = null
        machine = MachineEx(A, scope, debugLevel = DEBUG) {
            +FIRST set B
            +SECOND + CB set A
            +THIRD bind childMachine
            +!States exec {
                lastState = this.state as States
            }
        }

    }
}

class MachineExBindingTest : FunSpec({

    var ctx = TestContext()

    beforeEach {
        ctx = TestContext()
    }

    test("binding") {
        ctx.run {
            lastChildState shouldBe null
            lastState shouldBe null

            verify(FIRST, B, null)
            verify(THIRD0, CB)
            verify(SECOND, A, CB)
            verify(THIRD0, CB)
            verify(THIRD1, CC * Data0())
            verify(THIRD2, CA)
            verify(FIRST, B, CA)
        }
    }
})

private suspend fun TestContext.verify(event: AbsEvent, state: AbsState, childState: AbsState? = state) {
    machine.fire eventSync event shouldBe state.combo

    childMachine.stateData shouldBe childState?.slave
    lastChildState shouldBe childState?.master

    machine.stateData shouldBe state.slave
    lastState shouldBe state.master
}

val Master.slave get() = (this as? Combo<*>)?.slave