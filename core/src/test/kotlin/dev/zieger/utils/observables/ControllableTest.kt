package dev.zieger.utils.observables

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.observable.Controllable
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test

class ControllableTest {

    @Test
    fun `test controllable`() = runTest {
        val testObservable = Controllable("foo")
        testObservable.value assert "foo" % "1"

        testObservable.value = "boo"
        delay(100L)

        testObservable.value assert "boo" % "2"
    }

    @Test
    fun `test controllable inside class`() = runTest {
        val testClass = TestClass("foo")

        testClass.controllable.control { Log.d("111=$it"); if (value == "foo") value = "boo" }
        delay(100L)
        testClass.controllable.value assert "boo" % "1"
        testClass.value assert "boo" % "2"

        testClass.value = "moo"
        delay(100L)
        testClass.value assert "moo" % "3"
        testClass.controllable.value assert "moo" % "4"
    }

    @Test
    fun `test controllable inside class suspended`() = runTest {
        val testClass = TestClass("foo")

        testClass.controllable.controlS { Log.d("111=$it"); if (value == "foo") value = "boo" }
        delay(100L)
        testClass.controllable.value assert "boo" % "1"
        testClass.value assert "boo" % "2"

        testClass.value = "moo"
        delay(100L)
        testClass.value assert "moo" % "3"
        testClass.controllable.value assert "moo" % "4"
    }

    companion object {

        class TestClass(value: String) {
            val controllable = Controllable(
                value,
                notifyForExistingInternal = true,
                scope = DefaultCoroutineScope()
            )
            var value by controllable
        }
    }
}