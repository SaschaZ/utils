package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.coroutines.scope.IoCoroutineScope
import de.gapps.utils.log.Log
import de.gapps.utils.misc.asUnit
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.duration.seconds
import de.gapps.utils.time.duration.years
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals

abstract class ProcessorBaseTest(
    numParallel: Int = 16,
    private val testProducerAmount: Int = 1024 * numParallel
) : AnnotationSpec() {

    protected open lateinit var params: ProcessingParams
    protected open lateinit var testProducer: Producer<Int>
    protected open lateinit var testProcessors: List<Processor<Int, String>>
    protected open lateinit var testConsumer: Consumer<String>
    protected open var lastTimeValTimes: List<ITimeEx> =
        ArrayList(listOf(TimeEx(20.years.millis), TimeEx(25.years.millis), TimeEx(30.years.millis)))

    @Before
    internal fun before() = runBlocking {
        params = ProcessingParams(ParallelProcessingTypes.UNIQUE, Channel.RENDEZVOUS, DefaultCoroutineScope(), 8)

        testProducer = Producer {
            repeat(testProducerAmount) {
                send(it)
                Log.v("send $it")
            }
        }

        testProcessors

        testConsumer = Consumer {
            Log.v("received $it")
        }
    }.asUnit()
}

class ParallelTest : AnnotationSpec() {

    @Before
    fun before() {
        testProducer = Producer<Int> {
            repeat(testProducerAmount) { send(it) }
            close()
            Log.d("producing finished")
        }

        testProcessor0 = Processor {
            //            Log.d("0: $it")
            send("0: $it", time = lastTimeValTime0)
            lastTimeValTime0 += 1.seconds
        }
        testProcessor1 = Processor {
            //            Log.d("1: $it")
            send("1: $it", time = lastTimeValTime1)
            lastTimeValTime1 += 1.seconds
        }
        testProcessor2 = Processor {
            //            Log.d("2: $it")
            send("2: $it", time = lastTimeValTime2)
            lastTimeValTime2 += 1.seconds
        }
    }

    @Test
    fun testUniquePiped() = runBlocking {
        val numParallel = 3
        val params = ProcessingParams(
            ParallelProcessingTypes.SAME,
            Channel.RENDEZVOUS,
            IoCoroutineScope(),
            numParallel
        )

        val result = ArrayList<IPipeValue<String>>()
        testProducer + ParallelProcessor(params) {
            when (it) {
                0 -> testProcessor0
                1 -> testProcessor1
                else -> testProcessor2
            }
        } + Consumer<String>(params) {
            it.split(": ").run {
                result.add(rawValue)
            }
//            Log.d("result: $it")
        }

        assertEquals(result.size, testProducerAmount * 3)
        val idxGroups = result.groupBy { it.parallelIdx }
        assertEquals(idxGroups.keys.size, numParallel)
        idxGroups.values.forEach { list ->
            assertEquals(list.size, testProducerAmount)
        }
    }.asUnit()

    @Test
    fun testEqualPiped() = runBlocking {
        val numParallel = 3
        val params = ProcessingParams(
            ParallelProcessingTypes.SAME,
            Channel.RENDEZVOUS,
            IoCoroutineScope(),
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
            it.split(": ").run { resultBuffer.getOrPut(rawValue.parallelIdx) { ArrayList() }.add(get(1).toInt()) }
//            Log.d("result: $it")
        }
        assertEquals(resultBuffer.keys.size, numParallel)
        resultBuffer.values.map { assertEquals(it.size, testProducerAmount); it.sorted() }.forEach {
            assertEquals(it.first(), 0)
            assertEquals(it.last(), testProducerAmount - 1)
        }
    }.asUnit()

    @Test
    fun testUniqueStandalone() = runBlocking {
        val numParallel = 3
        val params = ProcessingParams(
            ParallelProcessingTypes.SAME,
            Channel.RENDEZVOUS,
            IoCoroutineScope(),
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
        assertEquals(result.size, testProducerAmount * 3)
        val idxGroups = result.groupBy { it.parallelIdx }
        assertEquals(idxGroups.keys.size, numParallel)
        idxGroups.values.forEach { list ->
            assertEquals(list.size, testProducerAmount)
        }
    }.asUnit()

    @Test
    fun testEqualStandalone() = runBlocking {
        val numParallel = 3
        val params = ProcessingParams(
            ParallelProcessingTypes.SAME,
            Channel.RENDEZVOUS,
            IoCoroutineScope(),
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
        assertEquals(result.size, testProducerAmount * numParallel)
        val idxGroups =
            result.groupBy { it.parallelIdx }.map { it.key to it.value.sorted() }.toMap()
        assertEquals(idxGroups.keys.size, numParallel)
        idxGroups.values.forEach { list ->
            assertEquals(list.size, testProducerAmount)
        }
    }.asUnit()
}
