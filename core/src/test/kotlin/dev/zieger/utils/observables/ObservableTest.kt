@file:Suppress("unused", "MemberVisibilityCanBePrivate", "CanBeParameter", "RemoveExplicitTypeArguments")

package dev.zieger.utils.observables

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.observable.Observable
import io.kotest.core.spec.style.AnnotationSpec


class ObservableTest : AnnotationSpec() {

    @Test
    fun testObservable() = runTest {
        var obs0ListenerValue = 0
        val obs0 = Observable(0, this) {
            obs0ListenerValue = it
        }
        var val0 by obs0
        var obs0Listener2Value = 0
        val obs0UnObserve = obs0.observe { obs0Listener2Value = it }

        val0 = 1

        obs0.suspendUntilNextChange() isEqual 1
        val0 isEqual 1
        obs0.value isEqual 1
        obs0ListenerValue isEqual 1
        obs0Listener2Value isEqual 1

        obs0UnObserve()

        val0 = 2

        obs0.suspendUntilNextChange() isEqual 2
        val0 isEqual 2
        obs0.value isEqual 2
        obs0ListenerValue isEqual 2
        obs0Listener2Value isEqual 1
    }
}