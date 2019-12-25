package de.gapps.utils.coroutines.channel

import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.duration.seconds
import de.gapps.utils.time.duration.years
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking

class ParallelTest : AnnotationSpec() {

    private val testProducer = Producer<Int> {
        repeat(50) { send(it, isLastSend = it == 49) }
    }

    private var lastTimeValTime0: ITimeEx = TimeEx(20.years.millis)
    private val testProcessor0 = Processor<Int, String> {
        println("0: $it")
        send("0: $it", time = lastTimeValTime0)
        lastTimeValTime0 += 1.seconds
    }
    private var lastTimeValTime1: ITimeEx = TimeEx(25.years.millis)
    private val testProcessor1 = Processor<Int, String> {
        println("1: $it")
        send("1: $it", time = lastTimeValTime1)
        lastTimeValTime1 += 1.seconds
    }
    private var lastTimeValTime2: ITimeEx = TimeEx(30.years.millis)
    private val testProcessor2 = Processor<Int, String> {
        println("2: $it")
        send("2: $it", time = lastTimeValTime2)
        lastTimeValTime2 += 1.seconds
    }

    @Test
    fun testIt() = runBlocking {
        val params = ParallelProcessingParams(ParallelProcessingTypes.EQUAL, 3)
        testProducer.produce().parallel(params) {
            when (it) {
                0 -> testProcessor0
                1 -> testProcessor1
                else -> testProcessor2
            }
        }.consumer(params) { println(it) }
    }.asUnit()
}
