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
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals

abstract class ProcessorBaseTest(
    protected val numParallel: Int = 16,
    protected val testProducerAmount: Int = 1024 * numParallel
) : AnnotationSpec() {

    protected open lateinit var params: ProcessingParams
    protected open lateinit var testProducer: Producer<Int>
    protected open lateinit var testProcessors: List<Processor<Int, String>>
    protected open lateinit var testConsumer: Consumer<String>
    protected open var lastTimeValTimes: List<ITimeEx> =
        ArrayList(listOf(TimeEx(20.years.millis), TimeEx(25.years.millis), TimeEx(30.years.millis)))

    @Before
    open fun before() = runBlocking {
        params = ProcessingParams(ParallelProcessingTypes.UNIQUE, Channel.RENDEZVOUS, DefaultCoroutineScope(), 8)

        testProducer = Producer {
            repeat(testProducerAmount) {
                send(it)
                Log.v("send $it")
            }
        }

        testConsumer = Consumer {
            Log.v("received $it")
        }
    }.asUnit()
}

class ParallelTest : ProcessorBaseTest() {

    @Before
    override fun before() {
        super.before()
        testProcessors = params.parallelIndices.map { idx ->
            Processor<Int, String>(params) { send("$it") }
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
            testProcessors[it]
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
            testProcessors[it]
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
                testProcessors[it]
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
                testProcessors[it]
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
