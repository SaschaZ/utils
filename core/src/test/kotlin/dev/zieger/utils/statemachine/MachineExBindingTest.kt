package dev.zieger.utils.statemachine

import dev.zieger.utils.core_testing.FlakyTest
import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.isNull
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.statemachine.MachineEx.Companion.DebugLevel.DEBUG
import dev.zieger.utils.statemachine.MachineExBindingTest.Events.*
import dev.zieger.utils.statemachine.MachineExBindingTest.Events.THIRD.*
import dev.zieger.utils.statemachine.MachineExBindingTest.States.*
import dev.zieger.utils.statemachine.MachineExBindingTest.States.C.*
import dev.zieger.utils.statemachine.MachineExBindingTest.TestData.*
import dev.zieger.utils.statemachine.conditionelements.*
import dev.zieger.utils.statemachine.dsl.times
import org.junit.jupiter.api.Test

class MachineExBindingTest : FlakyTest() {

    sealed class States : State() {
        object A : States()
        object B : States()
        sealed class C : States() {
            object CA : C()
            object CB : C()
            object CC : C()

            companion object : StateGroup<States>(States::class)
        }

        companion object : StateGroup<States>(States::class)
    }

    sealed class Events : Event() {
        object FIRST : Events()
        object SECOND : Events()
        sealed class THIRD : Events() {
            object THIRD0 : THIRD()
            object THIRD1 : THIRD()
            object THIRD2 : THIRD()

            companion object : EventGroup<THIRD>(THIRD::class)
        }

        companion object : EventGroup<Events>(Events::class)
    }

    sealed class TestData : Data {

        data class Data0(val test: Boolean = false) : TestData() {
            companion object : Type<Data0>(Data0::class)
        }

        data class Data1(val test: String = "") : TestData() {
            companion object : Type<Data1>(Data1::class)
        }

        companion object : Type<TestData>(TestData::class)
    }

    private lateinit var childMachine: MachineEx
    private lateinit var machine: MachineEx

    private var lastChildState: C? = null
    private var lastState: States? = null

    override suspend fun beforeEach() {
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

    private suspend fun verify(event: AbsEvent, state: AbsState, childState: AbsState? = state) {
        machine.fire eventSync event isEqual state.combo

        childMachine.stateData isEqual childState?.slave
        lastChildState isEqual childState?.master

        machine.stateData isEqual state.slave
        lastState isEqual state.master
    }

    val Master.slave get() = (this as? Combo<*>)?.slave

    @Test
    fun testBinding() = runTest {
        lastChildState.isNull()
        lastState.isNull()

        verify(FIRST, B, null)
        verify(THIRD0, CB)
        verify(SECOND, A, CB)
        verify(THIRD0, CB)
        verify(THIRD1, CC * Data0())
        verify(THIRD2, CA)
        verify(FIRST, B, CA)
    }
}