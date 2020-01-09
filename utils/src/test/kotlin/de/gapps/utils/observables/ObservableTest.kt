@file:Suppress("unused")

package de.gapps.utils.observables

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.equals
import de.gapps.utils.observable.Observable
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class ObservableTest : AnnotationSpec() {

    @Test
    fun `test observable inside class`() = runBlocking {
        val testClass = TestClass("foo", this)
        testClass.observable.value equals "foo"
        var latestObservedChanged: String = testClass.observable.value
        launchEx { testClass.observable.observe { latestObservedChanged = it } }

        launchEx { testClass.triggerChange("boo") }
        delay(100L)

        testClass.observable.value equals "boo"
        latestObservedChanged equals "boo"
    }

    companion object {

        class TestClass(value: String, scope: CoroutineScope) {
            val observable = Observable(value, scope)
            private var internal by observable

            fun triggerChange(value: String) {
                internal = value
            }
        }
    }
}