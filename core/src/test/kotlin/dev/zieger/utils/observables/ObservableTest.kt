@file:Suppress("unused")

package dev.zieger.utils.observables

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.log.Log
import dev.zieger.utils.misc.castSafe
import dev.zieger.utils.observable.Observable
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

fun parametersOf(params: List<Pair<String, List<*>>>, block: () -> Unit) {

}

abstract class ParameterHolder {
    val params: List<Pair<String, KClass<*>>> = this::class.memberProperties.mapNotNull {
        when (it.name) {
            "params" -> null
            else -> it.typeParameters.firstOrNull()?.upperBounds?.firstOrNull()?.classifier?.castSafe<KClass<*>>()
                ?.let { c -> it.name to c }
        }
    }
}

data class TestParameterHolder(
    val testVal0: List<Int> = listOf(0, 5),
    val testVal1: List<String> = listOf("foo"),
    val testVal2: List<Double> = listOf(10.5)
) : ParameterHolder()

class ParameterHolderTest {

    @Test
    fun testParameterHolder() = runTest {
        println("${TestParameterHolder().params}")
    }
}

class ObservableTest {

    @Test
    fun `test observable inside class`() = runTest {
        val testClass = TestClass("foo")
        testClass.observable.value assert "foo" % "1"
        testClass.value assert "foo" % "1b"

        var latestObservedChanged: String = testClass.observable.value
        testClass.observable.observe { Log.d("observe; value=$it"); latestObservedChanged = it }
        var latestObservedChangedS: String = testClass.observable.value
        testClass.observable.observeS { Log.d("observeS; value=$it"); latestObservedChangedS = it }
        testClass.value = "boo"
        delay(200L)

        testClass.observable.value assert "boo" % "2"
        testClass.value assert "boo" % "2b"
        latestObservedChanged assert "boo" % "3"
        latestObservedChangedS assert "boo" % "3"
    }

    companion object {

        class TestClass(value: String) {
            val observable = Observable(value, scope = DefaultCoroutineScope())
            var value by observable
        }
    }
}