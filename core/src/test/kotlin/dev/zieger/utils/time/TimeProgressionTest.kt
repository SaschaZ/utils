package dev.zieger.utils.time

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.time.base.div
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.base.times
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.minutes
import dev.zieger.utils.time.duration.weeks
import dev.zieger.utils.time.progression.step
import io.kotest.core.spec.style.FunSpec

class TimeProgressionTest : FunSpec({

    test("init") {
        val end: ITimeEx = TimeEx()
        val start: ITimeEx = end - 2.weeks
        val step: IDurationEx = 1.minutes
        (start..end step (step * 750)).toList().also {
            it.size assert (2.weeks / (step * 750)).toInt()
        }
    }
})