package dev.zieger.utils.core_testing

import dev.zieger.utils.core_testing.assertion2.isAnyOf
import dev.zieger.utils.core_testing.assertion2.isEqualOrNull
import dev.zieger.utils.core_testing.assertion2.isInRange
import dev.zieger.utils.core_testing.mix.ParamInstance
import dev.zieger.utils.core_testing.mix.bind
import dev.zieger.utils.core_testing.mix.param
import dev.zieger.utils.core_testing.mix.parameterMix
import org.junit.jupiter.api.Test

class TestParameterMix {

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
            param(TestVars::testVars0, 8..10 step 1),
            param(TestVars::testVars1, "foo", "boo"),
            param(TestVars::testVars2, 10.5, 12.4),
            param(TestVars::testVars3, "bäm", null)
        ) {
            testVars0 isInRange 8..10
            testVars1 isAnyOf listOf("foo", "boo")
            testVars2 isAnyOf listOf(10.5, 12.4)
            testVars3 isEqualOrNull "bäm"
        }
    }
}
