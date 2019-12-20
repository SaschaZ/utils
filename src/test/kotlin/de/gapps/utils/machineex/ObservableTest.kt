@file:Suppress("unused")

package de.gapps.utils.machineex

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.misc.asUnit
import de.gapps.utils.observable.Observable
import de.gapps.utils.observable.ObserverDelegate
import de.gapps.utils.observable.observe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

class ObservableTest : TestScopeTest() {

    @Test
    fun `test sync observing`() {
        val testObservable = Observable("foo", TestCoroutineScope())
        testObservable.value equals "foo"

        testObservable.value = "boo"

        testObservable.value equals "boo"
    }

    @Test
    fun `test async observing`() = runBlocking {
        val testObservable = Observable("foo", scope)
        testObservable.value equals "foo"
        var latestObservedChanged: String? = null
        scope.launchEx { for (item in testObservable.observe) latestObservedChanged = item }

        testObservable.value = "boo"

        latestObservedChanged equals "boo"
        testObservable.value equals "boo"
    }.asUnit()

    class TestClass(scope: CoroutineScope) {
        val observable = ObserverDelegate("foo", scope)
        private var internal by observable

        fun triggerChange() {
            internal = "boo"
        }
    }

    @Test
    fun `test sync delegate observing`() {
        val testClass = TestClass(scope)
        testClass.observable.value equals "foo"
        var latestObservedChanged: String? = null
        testClass.observable.observe { latestObservedChanged = it }

        testClass.triggerChange()

        latestObservedChanged equals "boo"
        testClass.observable.value equals "boo"
    }

    @Test
    fun `test async delegate observing`() = runBlocking {
        val testClass = TestClass(scope)
        testClass.observable.value equals "foo"
        var latestObservedChanged: String? = null
        scope.launchEx { for (item in testClass.observable.observe) latestObservedChanged = item }

        testClass.triggerChange()

        latestObservedChanged equals "boo"
        testClass.observable.value equals "boo"
    }.asUnit()
}