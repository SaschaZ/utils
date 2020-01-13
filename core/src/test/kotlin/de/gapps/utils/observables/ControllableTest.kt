package de.gapps.utils.observables

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.equals
import de.gapps.utils.observable.Controllable
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class ControllableTest : AnnotationSpec() {

    @Test
    fun `test controllable`() = runBlocking {
        val testObservable = Controllable("foo")
        testObservable.value equals "foo"

        launchEx { testObservable.value = "boo" }
        delay(100L)

        testObservable.value equals "boo"
    }

    @Test
    fun `test controllable inside class`() = runBlocking {
        val testClass = TestClass("foo")

        testClass.controllable.control { if (value == "foo") value = "boo" }
        delay(100L)
        testClass.internal equals "boo"
        testClass.controllable.value equals "boo"

        testClass.triggerChange("moo")
        delay(100L)
        testClass.internal equals "moo"
        testClass.controllable.value equals "moo"
    }

    companion object {

        class TestClass(value: String) {
            val controllable = Controllable(value, notifyForExisting = true)
            var internal by controllable
                private set

            fun triggerChange(value: String) {
                internal = value
            }
        }
    }
}