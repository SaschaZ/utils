@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.core_testing.assertion.assert
import de.gapps.utils.core_testing.runTest
import de.gapps.utils.coroutines.channel.parallel.ParallelProcessor
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.coroutines.scope.ICoroutineScopeEx
import de.gapps.utils.misc.asUnit
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

abstract class ProcessorBaseTest(
    protected val type: ParallelProcessingType = ParallelProcessingType.UNIQUE,
    protected val channelCapacity: Int = RENDEZVOUS,
    protected val numParallel: Int = 8,
    protected val testProducerAmount: Int = 128 * numParallel
) : IParamsHolder {

    protected open lateinit var scope: ICoroutineScopeEx
    protected open lateinit var testProducer: IProducer<Int>
    protected open lateinit var testProcessors: List<IProcessingUnit<Int, String>>
    protected open lateinit var testParallelProcessor: ParallelProcessor<Int, String>
    protected open lateinit var testConsumer: IConsumer<String>
    protected lateinit var consumeValues: ArrayList<IPipeValue<String>>
    override lateinit var params: IProcessingParams

    @BeforeEach
    open fun before() = runBlocking {
        scope = DefaultCoroutineScope()
        params = ProcessingParams(
            type,
            channelCapacity,
            numParallel
        )
        testProducer = producer<Int> {
            repeat(testProducerAmount) {
                val value = send(it)
//                Log.v("produce: send $value")
            }
            close()
        }

        testProcessors = params.parallelIndices.map {
            processor<Int, String> {
                val value1 = send("$value")
//                Log.v("process: $value1")
            }
        }
        testParallelProcessor = testProcessors.inParallel(params)

        consumeValues = ArrayList()
        testConsumer = consumer {
            //            Log.v("consume: received $rawValue")
            consumeValues.add(rawValue)
        }
    }.asUnit()

    @AfterEach
    open fun after() {
        scope.cancel()
    }

    @Test
    fun testBaseTest() = runTest {
        val testProcessor = processor<Int, String> { send("$it") }
        testConsumer.run { testProcessor.run { testProducer.produce().process().consume().join() } }

        consumeValues.size assert testProducerAmount
        consumeValues.forEachIndexed { index, i -> index assert i.inIdx }
    }
}

inline fun <reified I : Any, reified O : Any> Collection<IProcessingUnit<I, O>>.inParallel(params: IProcessingParams = ProcessingParams()) =
    ParallelProcessor(params) { toList()[it] }

inline fun <reified I : Any, reified O : Any> parallelWith(
    params: IProcessingParams = ProcessingParams(),
    crossinline factory: (Int) -> IProcessingUnit<I, O>
) = ParallelProcessor(params) { factory(it) }