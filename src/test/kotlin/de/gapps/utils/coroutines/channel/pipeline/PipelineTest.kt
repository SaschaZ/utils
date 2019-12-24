package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.consumer
import de.gapps.utils.coroutines.channel.processor
import de.gapps.utils.coroutines.channel.producer
import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

class PipelineTest : AnnotationSpec() {

    private val testProducer = producer<Int> {
        repeat(5) { send(it) }
        close()
    }

    private val testProcessor1 = processor<Int, Float> { send(it.toFloat()) }
    private val testProcessor2 = processor<Float, String> { send("$it") }
    private val testProcessor3 = processor<String, Int> { send(it.toFloat().toInt()) }

    @Test
    fun testPipeline() = runBlocking {
        val consumerResult = ArrayList<Int>()
        val testConsumer = consumer<Int> { consumerResult.add(it) }
        testProducer + testProcessor1 + testProcessor2 + testProcessor3 + testConsumer
        delay(100)
        assertEquals(5, consumerResult.size)
        consumerResult.forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }.asUnit()
}