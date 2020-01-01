package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConsumerTest : AnnotationSpec() {

    private val testProducer = Producer<Int> {
        repeat(5) { send(it) }
        close()
    }

    @Test
    fun testConsumer() = runBlocking {
        val consumerResult = ArrayList<Int>()
        var finished = false
        Consumer<Int> { consumerResult.add(it) }.run {
            onConsumingFinished = { finished = true }
            testProducer.produce().consume().join()
        }
        assertTrue(finished)
        assertEquals(5, consumerResult.size)
        consumerResult.forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }.asUnit()
}