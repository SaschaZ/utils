@file:Suppress("unused")

package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.rem
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.milliseconds
import io.kotest.core.spec.style.FunSpec
import java.util.concurrent.atomic.AtomicInteger

class TypeContinuationTest : FunSpec({

    lateinit var continuation: TypeContinuation<Int>
    val continued = AtomicInteger(0)

    beforeEach {
        continuation = TypeContinuation()
        continued.set(0)
    }

    test("direct") {
        var result: Int? = null
        launchEx {
            result = continuation.suspend()
            continued.incrementAndGet()
        }

        delay(100.milliseconds)
        continued.get() isEqual 0 % "0"
        continuation.resume(100)
        delay(100.milliseconds)
        continued.get() isEqual 1 % "1"
        result isEqual 100
    }

    test("multiple") {
        var result0: Int? = null
        launchEx {
            result0 = continuation.suspend()
            continued.incrementAndGet()
        }
        var result1: Int? = null
        launchEx {
            result1 = continuation.suspend()
            continued.incrementAndGet()
        }

        delay(100.milliseconds)
        continued.get() isEqual 0 % "0"
        continuation.resume(100)
        delay(100.milliseconds)
        continued.get() isEqual 2 % "1"
        result0 isEqual 100
        result1 isEqual 100

        var result2: Int? = null
        launchEx {
            result2 = continuation.suspend()
            continued.incrementAndGet()
        }

        delay(100.milliseconds)
        continued.get() isEqual 2 % "0"
        continuation.resume(200)
        delay(100.milliseconds)
        continued.get() isEqual 3 % "1"
        result2 isEqual 200
    }
})