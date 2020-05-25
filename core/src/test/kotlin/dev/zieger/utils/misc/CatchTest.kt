@file:Suppress("unused")

package dev.zieger.utils.misc

import dev.zieger.utils.core_testing.ParamInstance
import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.core_testing.assertion.rem
import dev.zieger.utils.core_testing.bind
import dev.zieger.utils.core_testing.param
import dev.zieger.utils.core_testing.parameterMix
import dev.zieger.utils.delegates.nextInt
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.random.Random

class CatchTest {

    data class CatchTestData(val map: Map<String, ParamInstance<*>>) {
        val result: Boolean by bind(map)
        val returnOnCatch: Boolean by bind(map)
        val maxExecutions: Int by bind(map)
        val printStackTrace: Boolean by bind(map)
        val logStackTrace: Boolean by bind(map)
        val throwException: Boolean by bind(map)
    }

    @Disabled // TODO implement with KoTest
    @Test
    fun testCatch() {
        parameterMix(
            { CatchTestData(it) },
            param("result", Random.nextBoolean()),
            param("returnOnCatch", Random.nextBoolean()),
            param("maxExecutions", Random.nextInt(0..9)),
            param("printStackTrace", Random.nextBoolean()),
            param("logStackTrace", Random.nextBoolean()),
            param("throwException", Random.nextBoolean())
        ) {
            var caught = false
            var finally = false
            val receivedResult = catch(returnOnCatch, maxExecutions, printStackTrace, logStackTrace,
                onCatch = { caught = true }, onFinally = { finally = true }) {
                if (throwException) throw RuntimeException("testException")
                result
            }

            caught assert throwException % "caught"
            finally assert true % "finally"
            receivedResult assert (if (throwException) returnOnCatch else result) % "result"
        }
    }
}