@file:Suppress("unused")

package de.gapps.utils.statemachine

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.assertion.onFail
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.misc.name
import de.gapps.utils.statemachine.MachineExTest.TestEvent.*
import de.gapps.utils.statemachine.MachineExTest.TestState.*
import de.gapps.utils.statemachine.scopes.on
import de.gapps.utils.statemachine.scopes.set
import de.gapps.utils.statemachine.scopes.withData
import org.junit.jupiter.api.Test


class MachineExTest {

    sealed class TestState : IState {
        override fun toString(): String = this::class.name

        object INITIAL : TestState()
        object A : TestState()
        object B : TestState()
        object C : TestState()
        object D : TestState()
    }

    data class TestData(val foo: String) : IData

    sealed class TestEvent : IEvent<TestData> {
        override var data: TestData? = null

        override fun toString(): String = this::class.name

        object FIRST : TestEvent()
        object SECOND : TestEvent()
        object THIRD : TestEvent()
        object FOURTH : TestEvent()
    }

    @Test
    fun testBuilder() = runTest {
        var executed = 0
        MachineEx<TestData, TestEvent, TestState>(INITIAL) {
            on event FIRST andState INITIAL set A
            on event FIRST + SECOND + THIRD andState A + B set C
            on event THIRD andState C execAndSet {
                executed++; (data as? TestData)?.foo onFail "data test" assert "foo"; D
            }
            on event FOURTH andState D set B
            on state C exec { executed++ }
        }.run {
            state assert INITIAL

            set eventSync FIRST
            state assert A
            executed assert 0

            set eventSync SECOND.withData(TestData("moo"))
            state assert C
            executed assert 1

            set eventSync (THIRD withData TestData("foo"))
            state assert D
            executed onFail "first" assert 2

            set eventSync FOURTH
            state assert B
            executed onFail "second" assert 2

            set eventSync FIRST
            state assert C
            executed onFail "third" assert 3
        }
    }

    @Test
    fun testBuilderSyncSet() = runTest {
        MachineEx<IData, TestEvent, TestState>(INITIAL) {
            on event FIRST andState INITIAL set A
        }.run {
            state assert INITIAL

            set event FOURTH
            set event THIRD
            set event FOURTH
            set event THIRD
            set event FOURTH
            set event THIRD
            set event FOURTH
            set eventSync FIRST
            state assert A
        }
    }
}
