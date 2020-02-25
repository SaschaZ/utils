package de.gapps.utils.observables

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.log.Log
import de.gapps.utils.observable.Controllable
import de.gapps.utils.testing.assertion.assert
import de.gapps.utils.testing.assertion.onFail
import de.gapps.utils.testing.runTest
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.delay

class ControllableTest : AnnotationSpec() {

    @Test
    fun `test controllable`() = runTest {
        val testObservable = Controllable("foo")
        testObservable.value onFail "1" assert "foo"

        launchEx { testObservable.value = "boo" }
        delay(100L)

        testObservable.value onFail "2" assert "boo"
    }

    @Test
    fun `test controllable inside class`() = runTest {
        val testClass = TestClass("foo")

        testClass.controllable.control { Log.d("111=$it");if (value == "foo") value = "boo" }
        delay(100L)
        testClass.controllable.value onFail "1" assert "boo"
        testClass.internal onFail "2" assert "boo"

        testClass.internal = "moo"
        delay(100L)
        testClass.internal onFail "3" assert "moo"
        testClass.controllable.value onFail "4" assert "moo"
    }

    companion object {

        class TestClass(value: String) {
            val controllable = Controllable(value, notifyForExisting = true)
            var internal by controllable
        }
    }
}