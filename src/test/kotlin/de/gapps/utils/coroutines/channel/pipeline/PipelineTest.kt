package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.log.Log
import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.random.Random
import kotlin.test.assertEquals

class PipelineTest : AnnotationSpec() {

    private val testProducer = Producer<String> {
        repeat(30) { send("$it") }
        close()
    }

    private val inputProcessor = Processor<String, Long> { randomDelay(); send(it.toLong()) }

    private val parallelProcessor0 = Processor<Long, Int> { randomDelay(); send(it.toInt()) }
    private val parallelProcessor1 = Processor<Long, Double> { randomDelay(); send(it.toDouble()) }
    private val parallelProcessor2 = Processor<Long, String> { randomDelay(); send(it.toString()) }

    private val parallelTestProcessor = ParallelProcessor(
        ParallelProcessingParams(
            ParallelProcessingTypes.UNIQUE
        )
    ) {
        when (it) {
            0 -> parallelProcessor0
            1 -> parallelProcessor1
            else -> parallelProcessor2
        }
    }

    private fun groupByParallelIdx() = Processor<Any, Pair<Int, Any>> {
        send((value as IParallelNodeValue<*>).parallelIdx to it)
    }

    private val outputProcessor = Processor<Pair<Int, Any>, Int> { value ->
        send(
            when (value.first) {
                0 -> (value.second as Int)
                1 -> (value.second as Double).toInt()
                else -> (value.second as String).toInt()
            }
        )
    }

    private suspend fun randomDelay() = delay(Random(System.currentTimeMillis()).nextLong(10L, 30L))

    @Test
    fun testPipeline() = runBlocking {
        withTimeout(2000L) {
            val consumerResult = ArrayList<Int>()
            val testConsumer = Consumer<Int> { consumerResult.add(it); Log.v("consume: $it") }
            testProducer + inputProcessor + parallelTestProcessor + groupByParallelIdx() + outputProcessor + testConsumer
            assertEquals(30, consumerResult.size)
            consumerResult.sorted().forEachIndexed { index, i ->
                assertEquals(index, i)
            }
        }
    }.asUnit()
}