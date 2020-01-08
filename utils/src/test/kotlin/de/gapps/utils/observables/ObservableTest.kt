@file:Suppress("unused")

package de.gapps.utils.observables

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.equals
import de.gapps.utils.misc.asUnit
import de.gapps.utils.observable.Observable
import de.gapps.utils.observable.ObserverDelegate
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class ObservableTest : AnnotationSpec() {

    @Test
    fun `test sync observing`() = runBlocking {
        val testObservable = Observable("foo", this)
        testObservable.value equals "foo"

        launchEx { testObservable.value = "boo" }
        delay(100L)

        testObservable.value equals "boo"
    }

    @Test
    fun `test async observing`() = runBlocking {
        val testObservable = Observable("foo", this)
        testObservable.value equals "foo"
        var latestObservedChanged: String = testObservable.value
        val receiver = Channel<String>()
        launchEx {
            testObservable.observe(receiver)
            for (item in receiver) latestObservedChanged = item
        }

        testObservable.value = "boo"
        delay(100L)
        receiver.close()

        latestObservedChanged equals "boo"
        testObservable.value equals "boo"
    }.asUnit()

    class TestClass(value: String, scope: CoroutineScope) {
        val observable = ObserverDelegate(value, scope)
        private var internal by observable

        fun triggerChange(value: String) {
            internal = value
        }
    }

    @Test
    fun `test sync delegate observing`() = runBlocking {
        val testClass = TestClass("foo", this)
        testClass.observable.value equals "foo"
        var latestObservedChanged: String? = null
        launchEx { testClass.observable.observe { latestObservedChanged = it } }

        launchEx { testClass.triggerChange("boo") }
        delay(100L)

        testClass.observable.value equals "boo"
        latestObservedChanged equals "boo"
    }

    @Test
    fun `test async delegate observing`() = runBlocking {
        val testClass = TestClass("foo", this)
        testClass.observable.value equals "foo"
        var latestObservedChanged: String = testClass.observable.value
        val receiver = Channel<String>()
        launchEx {
            testClass.observable.observe(receiver)
            for (item in receiver) latestObservedChanged = item
        }

        testClass.triggerChange("boo")
        delay(100L)
        receiver.close()

        testClass.observable.value equals "boo"
        latestObservedChanged equals "boo"
    }.asUnit()
}