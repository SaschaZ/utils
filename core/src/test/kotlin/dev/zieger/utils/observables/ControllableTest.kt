package dev.zieger.utils.observables

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.onFail
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.log.Log
import dev.zieger.utils.observable.Controllable
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test

class ControllableTest {

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