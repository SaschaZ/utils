package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import io.mockk.mockk
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProducerTest : AnnotationSpec() {

    @Test
    fun testProducer() = runBlocking {
        val producer = Producer<Int> {
            repeat(5) { send(it) }
            close()
        }
        var finished = false
        producer.onProducingFinished = { finished = true }
        producer.pipeline = mockk<IPipeline<Int, Int>>()
        val producerResult = producer.produce().toList()

        assertTrue(finished)
        assertEquals(5, producerResult.size)
        producerResult.forEachIndexed { index, i ->
            assertEquals(index, i.value)
        }
    }.asUnit()
}