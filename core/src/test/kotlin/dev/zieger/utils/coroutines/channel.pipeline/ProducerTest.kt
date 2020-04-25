package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.misc.asUnit
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class ProducerTest {

    @Test
    fun testProducer() = runBlocking {
        var finished = false
        val producer = object : Producer<Int>(ProcessingParams(), produce = {
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

        finished assert true
        producerResult.size assert 5
        producerResult.forEachIndexed { index, i ->
            i.value assert index
            i.outIdx assert index
        }
    }.asUnit()
}