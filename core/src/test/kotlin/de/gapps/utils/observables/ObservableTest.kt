@file:Suppress("unused")

package de.gapps.utils.observables

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.equals
import de.gapps.utils.observable.Observable
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class ObservableTest : AnnotationSpec() {

    @Test
    fun `test observable inside class`() = runBlocking {
        val testClass = TestClass("foo")
        testClass.observable.value equals "foo"
        var latestObservedChanged: String = testClass.observable.value
        launchEx { testClass.observable.observe { latestObservedChanged = it } }

        launchEx { testClass.triggerChange("boo") }
        delay(100L)

        testClass.observable.value equals "boo"
        latestObservedChanged equals "boo"
        testClass.success equals true
    }

    companion object {

        class TestClass(value: String) {
            var success: Boolean = false
                private set
            val observable = Observable(value)
            private var internal by observable.apply {
                observe { success = true }
            }

            fun triggerChange(value: String) {
                internal = value
            }
        }
    }
}