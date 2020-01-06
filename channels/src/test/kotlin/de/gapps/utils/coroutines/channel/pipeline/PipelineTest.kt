package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.log.Log
import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.assertEquals

class PipelineTest : AnnotationSpec() {

    private val testValueAmount = 1000L
    private val testProducer = Producer<String> {
        repeat(testValueAmount.toInt()) { send("$it") }
        close()
    }

    private val inputProcessor = Processor<String, Long> { randomDelay(); send(it.toLong()) }

    private val parallelProcessors = (0 until 8).map { Processor<Long, Double> { randomDelay(); send(it.toDouble()) } }

    private val parallelTestProcessor = ParallelProcessor(
        ParallelProcessingParams(
            ParallelProcessingTypes.UNIQUE
        )
    ) { parallelProcessors[it] }

    private suspend fun randomDelay() = Unit//delay(Random(System.currentTimeMillis()).nextLong(1L, 3L))

    @Test
    fun testPipeline() = runBlocking {
        withTimeout(2000L) {
            Log.p(max = testValueAmount) {
                val consumerResult = ArrayList<Int>()
                val testConsumer = Consumer<Int> { consumerResult.add(it); step() }
                testProducer + inputProcessor + parallelTestProcessor + testConsumer
                assertEquals(testValueAmount, consumerResult.size.toLong())
                consumerResult.sorted().forEachIndexed { index, i ->
                    assertEquals(index, i)
                }
            }
        }
    }.asUnit()
}