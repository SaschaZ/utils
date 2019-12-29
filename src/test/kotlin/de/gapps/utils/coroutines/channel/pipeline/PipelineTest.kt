package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.*
import de.gapps.utils.log.Log
import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

class PipelineTest : AnnotationSpec() {

    private val testProducer = Producer<Int> {
        repeat(5) { send(it, isLastSend = it == 4) }
        Log.d("producing finished")
    }

    private val testProcessor1 =
        Processor<Int, Float> { send(it.toFloat()) }
    private val testProcessor2 =
        Processor<Float, String> { send("$it") }
    private val testProcessor3 =
        Processor<String, Int> { send(it.toFloat().toInt()) }

    private val parallelTestProcessor = ParallelProcessor(ParallelProcessingParams(ParallelProcessingTypes.UNIQUE)) {
        when (it) {
            0 -> testProcessor2
            1 -> testProcessor2
            else -> testProcessor2
        }
    }

    @Test
    fun testPipeline() = runBlocking {
        val consumerResult = ArrayList<Int>()
        val testConsumer = Consumer<Int> { consumerResult.add(it); Log.v("consume: $it") }
        testProducer + testProcessor1 + parallelTestProcessor + testProcessor3 + testConsumer
        assertEquals(5, consumerResult.size)
        consumerResult.sorted().forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }.asUnit()
}