package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.misc.asUnit
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConsumerTest {

    private val testProducer = Producer<Int> {
        repeat(5) { send(it) }
        close()
    }

    @Test
    fun testConsumer() = runBlocking {
        val consumerResult = ArrayList<Int>()
        var finished = false
        (object : Consumer<Int>(ProcessingParams(), { consumerResult.add(it) }) {
            override suspend fun IConsumerScope<Int>.onConsumingFinished() {
                finished = true
            }
        }).run { testProducer.produce().consume().join() }
        assertTrue(finished)
        assertEquals(5, consumerResult.size)
        consumerResult.forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }.asUnit()
}