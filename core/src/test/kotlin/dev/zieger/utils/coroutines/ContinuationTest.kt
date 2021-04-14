@file:Suppress("unused")

package dev.zieger.utils.coroutines

import dev.zieger.utils.core_testing.assertion2.isEqual
import dev.zieger.utils.core_testing.assertion2.rem
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.seconds
import io.kotest.core.spec.style.FunSpec
import java.util.concurrent.atomic.AtomicInteger

class ContinuationTest : FunSpec({

    lateinit var continuation: Continuation
    val continued = AtomicInteger(0)

    beforeEach {
        continuation = Continuation()
        continued.set(0)
    }

    test("direct") {
        launchEx {
            continuation.suspend()
            continued.incrementAndGet()
        }

        delay(1.seconds)
        continued.get() isEqual 0 % "0"
        continuation.resume()
        delay(2.seconds)
        continued.get() isEqual 1 % "1"
    }

    test("multiple") {
        launchEx {
            continuation.suspend()
            continued.incrementAndGet()
        }
        launchEx {
            continuation.suspend()
            continued.incrementAndGet()
        }

        delay(1.seconds)
        continued.get() isEqual 0 % "0"
        continuation.resume()
        delay(2.seconds)
        continued.get() isEqual 2 % "1"
    }
})