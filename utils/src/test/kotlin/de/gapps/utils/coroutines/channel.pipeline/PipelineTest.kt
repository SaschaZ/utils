package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.scope.IoCoroutineScope
import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.assertEquals

class PipelineTest : AnnotationSpec() {

    private val testValueAmount = 1000 * 8
    private val testProducer = Producer<String> {
        repeat(testValueAmount) { send("$it") }
        close()
    }

    private val inputProcessor = Processor<String, Long> { send(it.toLong()) }

    private val parallelProcessors = (0 until 8).map { Processor<Long, Double> { send(it.toDouble()) } }

    private val parallelTestProcessor = ParallelProcessor(
        ProcessingParams(
            ParallelProcessingTypes.UNIQUE,
            8,
            IoCoroutineScope()
        )
    ) { parallelProcessors[it] }

    @Test
    fun testPipeline() = runBlocking {
        withTimeout(2000L) {
            val consumerResult = ArrayList<Int>()
            val testConsumer = Consumer<Int> { consumerResult.add(it) }
            testProducer + inputProcessor + parallelTestProcessor + testConsumer
            assertEquals(testValueAmount, consumerResult.size)
            consumerResult.sorted().forEachIndexed { index, i ->
                assertEquals(index, i)
            }
        }
    }.asUnit()
}