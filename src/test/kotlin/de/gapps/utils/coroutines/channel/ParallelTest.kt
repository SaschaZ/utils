package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.channel.pipeline.plus
import de.gapps.utils.log.Log
import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.delay
import de.gapps.utils.time.duration.seconds
import de.gapps.utils.time.duration.years
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals

class ParallelTest : AnnotationSpec() {

    private val testProducerAmount = 9
    private lateinit var testProducer: IProducer<Int>

    private var lastTimeValTime0: ITimeEx = TimeEx(20.years.millis)
    private lateinit var testProcessor0: IProcessor<Int, String>
    private var lastTimeValTime1: ITimeEx = TimeEx(25.years.millis)
    private lateinit var testProcessor1: IProcessor<Int, String>
    private var lastTimeValTime2: ITimeEx = TimeEx(30.years.millis)
    private lateinit var testProcessor2: IProcessor<Int, String>

    @Before
    fun before() {
        testProducer = Producer {
            repeat(testProducerAmount) { send(it) }
            close()
            Log.d("producing finished")
        }

        testProcessor0 = Processor {
            Log.d("0: $it")
            send("0: $it", time = lastTimeValTime0)
            lastTimeValTime0 += 1.seconds
        }
        testProcessor1 = Processor {
            Log.d("1: $it")
            send("1: $it", time = lastTimeValTime1)
            lastTimeValTime1 += 1.seconds
        }
        testProcessor2 = Processor {
            Log.d("2: $it")
            send("2: $it", time = lastTimeValTime2)
            lastTimeValTime2 += 1.seconds
        }
    }

    @Test
    fun testUniquePiped() = runBlocking {
        val numParallel = 3
        val params = ParallelProcessingParams(
            ParallelProcessingTypes.UNIQUE,
            numParallel
        )
        testProducer + ParallelProcessor(params) {
            when (it) {
                0 -> testProcessor0
                1 -> testProcessor1
                else -> testProcessor2
            }
        } + Consumer<String>(params) {
            it.split(": ").run { assertEquals(first().toInt(), get(1).toInt() % numParallel) }
            Log.d("result: $it")
        }
    }.asUnit()

    @Test
    fun testEqualPiped() = runBlocking {
        val numParallel = 3
        val params = ParallelProcessingParams(
            ParallelProcessingTypes.EQUAL,
            numParallel
        )
        val resultBuffer = ConcurrentHashMap<Int, ArrayList<Int>>()
        testProducer + ParallelProcessor(params) {
            when (it) {
                0 -> testProcessor0
                1 -> testProcessor1
                else -> testProcessor2
            }
        } + Consumer<String>(params) {
            it.split(": ").run { resultBuffer.getOrPut(first().toInt()) { ArrayList() }.add(get(1).toInt()) }
            Log.d("result: $it")
        }
        delay(1.seconds)
        assertEquals(resultBuffer.keys.size, numParallel)
        resultBuffer.values.map { assertEquals(it.size, testProducerAmount); it.sorted() }.forEach {
            assertEquals(it.first(), 0)
            assertEquals(it.last(), testProducerAmount - 1)
        }
    }.asUnit()

    @Test
    fun testUniqueStandalone() = runBlocking {
        val numParallel = 3
        val params = ParallelProcessingParams(
            ParallelProcessingTypes.UNIQUE,
            numParallel
        )
        val result = testProducer.produce().let { producerChan ->
            ParallelProcessor(params) {
                when (it) {
                    0 -> testProcessor0
                    1 -> testProcessor1
                    else -> testProcessor2
                }
            }.run { producerChan.process() }.toList()
        }
        assertEquals(result.size, testProducerAmount)
        val idxGroups = result.groupBy { it.parallelIdx }
        assertEquals(idxGroups.keys.size, numParallel)
        idxGroups.keys.forEach { idx ->
            idxGroups[idx]?.also { list ->
                assertEquals(list.size, testProducerAmount / numParallel)
            }
        }
    }.asUnit()
}
