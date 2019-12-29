package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.channel.pipeline.plus
import de.gapps.utils.log.Log
import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.duration.seconds
import de.gapps.utils.time.duration.years
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking

class ParallelTest : AnnotationSpec() {

    private val testProducer = producer<Int> {
        repeat(9) { send(it) }
        close()
        Log.d("producing finished")
    }

    private var lastTimeValTime0: ITimeEx = TimeEx(20.years.millis)
    private val testProcessor0 = processor<Int, String> {
        Log.d("0: $it")
        send("0: $it", time = lastTimeValTime0)
        lastTimeValTime0 += 1.seconds
    }
    private var lastTimeValTime1: ITimeEx = TimeEx(25.years.millis)
    private val testProcessor1 = processor<Int, String> {
        Log.d("1: $it")
        send("1: $it", time = lastTimeValTime1)
        lastTimeValTime1 += 1.seconds
    }
    private var lastTimeValTime2: ITimeEx = TimeEx(30.years.millis)
    private val testProcessor2 = processor<Int, String> {
        Log.d("2: $it")
        send("2: $it", time = lastTimeValTime2)
        lastTimeValTime2 += 1.seconds
    }

    @Test
    fun testIt() = runBlocking {
        val params = ParallelProcessingParams(ParallelProcessingTypes.UNIQUE, 3)
        testProducer + parallel(params) {
            when (it) {
                0 -> testProcessor0
                1 -> testProcessor1
                else -> testProcessor2
            }
        } + consumer<String>(params) { Log.d("result: $it") }
    }.asUnit()
}
