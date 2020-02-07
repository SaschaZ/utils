package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.misc.asUnit
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProducerTest {

    @Test
    fun testProducer() = runBlocking {
        var finished = false
        val producer = object : Producer<Int>(ProcessingParams(), block = {
            repeat(5) { send(it) }
            close()
        }) {
            override suspend fun IProducerScope<Int>.onProducingFinished() {
                finished = true
            }
        }
        producer.pipeline = mockk()
        every { producer.pipeline.tick(any(), any(), any()) } returns Unit
        val producerResult = producer.produce().toList()

        assertTrue(finished)
        assertEquals(5, producerResult.size)
        producerResult.forEachIndexed { index, i ->
            assertEquals(index, i.value)
            assertEquals(index, i.outIdx)
        }
    }.asUnit()
}