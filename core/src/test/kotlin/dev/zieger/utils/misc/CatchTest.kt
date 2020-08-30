@file:Suppress("unused")

package dev.zieger.utils.misc

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.assertion2.AssertType
import dev.zieger.utils.core_testing.mix.ParamInstance
import dev.zieger.utils.core_testing.mix.bind
import dev.zieger.utils.core_testing.mix.param
import dev.zieger.utils.core_testing.mix.parameterMix
import dev.zieger.utils.delegates.nextInt
import io.kotlintest.specs.AnnotationSpec
import kotlin.random.Random

class CatchTest : AnnotationSpec() {

    data class CatchTestData(val map: Map<String, ParamInstance<*>>) {
        val result: Int by bind(map)
        val returnOnCatch: Int by bind(map)
        val maxExecutions: Int by bind(map)
        val printStackTrace: Boolean by bind(map)
        val logStackTrace: Boolean by bind(map)
        val throwException: Boolean by bind(map)
    }

    @Test
    fun testCatch() {
        parameterMix(
            { CatchTestData(it) },
            param("result", Random.nextInt()),
            param("returnOnCatch", Random.nextInt()),
            param("maxExecutions", Random.nextInt(0..9)),
            param("printStackTrace", true, false),
            param("logStackTrace", true, false),
            param("throwException", true, false)
        ) {
            var caught = false
            var finally = false
            val receivedResult = catch(returnOnCatch, maxExecutions, printStackTrace, logStackTrace,
                onCatch = { caught = true }, onFinally = { finally = true }) {
                if (throwException) throw RuntimeException("testException")
                result
            }

            caught to (throwException && maxExecutions > 0) assert AssertType.EQUALS % "caught"
            finally assert true % "finally"
            receivedResult assert (if (throwException) returnOnCatch else result) % "result"
        }
    }
}