package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.*

enum class TestState2 : AbsState by State() {
    A, B, C;

    companion object : StateGroup<TestState2>(TestState2::class)
}

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

sealed class TestData : Data {

    data class TestEventData(val foo: String) : TestData() {
        companion object : Type<TestEventData>(TestEventData::class)
    }

    data class TestEventData2(val foo: String) : TestData() {
        companion object : Type<TestEventData2>(TestEventData2::class)
    }

    data class TestStateData(val moo: Boolean) : TestData() {
        companion object : Type<TestStateData>(TestStateData::class)
    }

    data class Data0(val test: Boolean = false) : TestData() {
        companion object : Type<Data0>(Data0::class)
    }

    data class Data1(val test: String = "") : TestData() {
        companion object : Type<Data1>(Data1::class)
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
