@file:Suppress("unused")

package dev.zieger.utils.misc

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.isNull
import dev.zieger.utils.core_testing.assertion2.rem
import dev.zieger.utils.core_testing.mix.ParamInstance
import dev.zieger.utils.core_testing.mix.bind
import dev.zieger.utils.core_testing.mix.param
import dev.zieger.utils.core_testing.mix.parameterMix
import dev.zieger.utils.core_testing.runTest
import dev.zieger.utils.delegates.nextInt
import org.junit.jupiter.api.Test
import kotlin.random.Random

class CatchTest {

    data class CatchTestData(val map: Map<String, ParamInstance<*>>) {
        val result: Int by bind(map)
        val returnOnCatch: Int by bind(map)
        val maxExecutions: Int by bind(map)
        val printStackTrace: Boolean by bind(map)
        val logStackTrace: Boolean by bind(map)
    }

    @Test
    fun testCatch() {
        parameterMix(
            { CatchTestData(it) },
            param("result", 10) { Random.nextInt() },
            param("returnOnCatch", 10) { Random.nextInt() },
            param("maxExecutions", Random.nextInt(0..9)),
            param("printStackTrace", false, false),
            param("logStackTrace", true, false)
        ) {
            var caught = 0
            var finally = 0
            var throwException = false
            var numThrown = 0

            val receivedResult = catch(returnOnCatch, maxExecutions,
                printStackTrace = printStackTrace, logStackTrace = logStackTrace,
                onCatch = { caught++ }, onFinally = { finally++ }) {

                throwException = Random.nextBoolean()
                if (throwException) {
                    numThrown++
                    throw RuntimeException("testException")
                }
                result
            }

            caught isEqual numThrown % "caught"
            finally isEqual 1 % "finally"
            receivedResult isEqual (if (throwException || maxExecutions == 0) returnOnCatch else result) %
                    "result; throwException=$throwException; maxExecutions=$maxExecutions"
        }
    }

    @Test
    fun testNullReturn() = runTest {
        val result: Int? = catch(null) { throw Exception("foo") }
        result.isNull()
    }
}
