package dev.zieger.utils.statemachine

import dev.zieger.utils.statemachine.conditionelements.*
import io.kotlintest.specs.AnnotationSpec

class MachineExBindingTest : AnnotationSpec() {

    sealed class States : State() {
        object A : States()
        object B : States()
        sealed class C : States() {
            object CA : C()
            object CB : C()
            object CC : C()
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
        }

        companion object : EventGroup<Events>(Events::class)
    }

    sealed class TestData : Data() {

        data class Data0(val test: Boolean = false) : TestData() {
            companion object : Type<Data0>(Data0::class)
        }

        data class Data1(val test: String = "") : TestData() {
            companion object : Type<Data1>(Data1::class)
        }

        companion object : Type<TestData>(TestData::class)
    }

//    val machine = MachineEx(A) {
//        +Events.FIRST bind childMachine
//    }

    lateinit var childMachine: MachineEx
}