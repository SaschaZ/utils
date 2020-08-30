package dev.zieger.utils.core_testing

import dev.zieger.utils.core_testing.mix.ParamInstance
import dev.zieger.utils.core_testing.mix.bind
import dev.zieger.utils.core_testing.mix.param
import dev.zieger.utils.core_testing.mix.parameterMix
import io.kotlintest.specs.AnnotationSpec

class TestParameterMix : AnnotationSpec() {

    data class TestVars(val map: Map<String, ParamInstance<*>>) {

        val testVars0: Int by bind(map)
        val testVars1: String by bind(map)
        val testVars2: Double by bind(map)
        val testVars3: String? by bind(map)
    }

    @Test
    fun testMix() = runTest {
        parameterMix(
            { TestVars(it) },
            param("testVars0", 8..10 step 1),
            param("testVars1", "foo", "boo"),
            param("testVars2", 10.5, 12.4),
            param("testVars3", "bäm", null)
        ) {
            println(this)
        }
    }
}