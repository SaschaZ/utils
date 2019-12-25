package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.Consumer
import de.gapps.utils.coroutines.channel.Processor
import de.gapps.utils.coroutines.channel.Producer
import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

class PipelineTest : AnnotationSpec() {

    private val testProducer = Producer<Int> {
        repeat(5) { send(it, isLastSend = it == 4) }
    }

    private val testProcessor1 =
        Processor<Int, Float> { send(it.toFloat()) }
    private val testProcessor2 =
        Processor<Float, String> { send("$it") }
    private val testProcessor3 =
        Processor<String, Int> { send(it.toFloat().toInt()) }

    @Test
    fun testPipeline() = runBlocking {
        val consumerResult = ArrayList<Int>()
        val testConsumer =
            Consumer<Int> { consumerResult.add(it) }
        testProducer + testProcessor1 + testProcessor2 + testProcessor3 + testConsumer
        assertEquals(5, consumerResult.size)
        consumerResult.forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }.asUnit()
}