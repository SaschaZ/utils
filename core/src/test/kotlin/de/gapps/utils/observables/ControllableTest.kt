package de.gapps.utils.observables

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.observable.Controllable
import de.gapps.utils.testing.assertion.assert
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class ControllableTest : AnnotationSpec() {

    @Test
    fun `test controllable`() = runBlocking {
        val testObservable = Controllable("foo")
        testObservable.value assert "foo"

        launchEx { testObservable.value = "boo" }
        delay(100L)

        testObservable.value assert "boo"
    }

    @Test
    fun `test controllable inside class`() = runBlocking {
        val testClass = TestClass("foo")

        testClass.controllable.control { if (value == "foo") value = "boo" }
        delay(100L)
        testClass.internal assert "boo"
        testClass.controllable.value assert "boo"

        testClass.triggerChange("moo")
        delay(100L)
        testClass.internal assert "moo"
        testClass.controllable.value assert "moo"
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