@file:Suppress("ClassName", "unused", "SpellCheckingInspection")

package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.ConditionElement.Master.Group.EventGroup
import dev.zieger.utils.statemachine.ConditionElement.Master.Group.StateGroup
import dev.zieger.utils.statemachine.ConditionElement.Master.Single.Event
import dev.zieger.utils.statemachine.ConditionElement.Master.Single.State
import dev.zieger.utils.statemachine.ConditionElement.Slave.Data

sealed class TestState : State() {

    object INITIAL : TestState()
    object A : TestState()
    object B : TestState()
    object C : TestState()

    sealed class TEST_STATE_GROUP_DEFG : TestState() {
        object D : TEST_STATE_GROUP_DEFG()
        object E : TEST_STATE_GROUP_DEFG()

        sealed class TEST_STATE_GROUP_FG : TestState() {
            object F : TEST_STATE_GROUP_FG()
            object G : TEST_STATE_GROUP_FG()

            companion object : StateGroup<TEST_STATE_GROUP_FG>(TEST_STATE_GROUP_FG::class)
        }

        companion object : StateGroup<TEST_STATE_GROUP_DEFG>(TEST_STATE_GROUP_DEFG::class)
    }

    sealed class TEST_STATE_GROUP_HI : TestState() {
        object H : TEST_STATE_GROUP_HI()
        object I : TEST_STATE_GROUP_HI()

        companion object : StateGroup<TEST_STATE_GROUP_HI>(TEST_STATE_GROUP_HI::class)
    }

    companion object : StateGroup<TestState>(TestState::class)
}

sealed class TestData : Data() {

    data class TestEventData(val foo: String) : TestData() {
        companion object : Type<TestEventData>(TestEventData::class)
    }

    data class TestEventData2(val foo: String) : TestData() {
        companion object : Type<TestEventData2>(TestEventData2::class)
    }

    data class TestStateData(val moo: Boolean) : TestData() {
        companion object : Type<TestStateData>(TestStateData::class)
    }

    companion object : Type<TestData>(TestData::class)
}

sealed class TestEvent : Event() {

    object FIRST : TestEvent()
    object SECOND : TestEvent()
    object THIRD : TestEvent()

    sealed class TEST_EVENT_GROUP : TestEvent() {
        object FOURTH : TEST_EVENT_GROUP()
        object FIFTH : TEST_EVENT_GROUP()
        object SIXTH : TEST_EVENT_GROUP()

        companion object : EventGroup<TEST_EVENT_GROUP>(TEST_EVENT_GROUP::class)
    }

    companion object : EventGroup<TestEvent>(TestEvent::class)
}

sealed class TestChildState : State() {

    object CINITIAL : TestChildState()
    object CA : TestChildState()
    object CB : TestChildState()
    object CC : TestChildState()
    object CD : TestChildState()

    companion object : StateGroup<TestChildState>(TestChildState::class)
}

sealed class TestChildEvent : Event() {

    object CFIRST : TestChildEvent()
    object CSECOND : TestChildEvent()
    object CTHIRD : TestChildEvent()

    companion object : EventGroup<TestChildEvent>(TestChildEvent::class)
}

sealed class TestChildData : Data() {

    data class Data0(val foo: Boolean = true) : TestChildData() {
        companion object : Type<Data0>(Data0::class)
    }

    data class Data1(val boo: Long = 0L) : TestChildData() {
        companion object : Type<Data1>(Data1::class)
    }

    data class Data2(val moo: Double = 0.0) : TestChildData() {
        companion object : Type<Data2>(Data2::class)
    }

    data class Data3(val woo: String = "") : TestChildData() {
        companion object : Type<Data3>(Data3::class)
    }

    companion object : Type<TestChildData>(TestChildData::class)
}