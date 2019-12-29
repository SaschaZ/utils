package de.gapps.utils.coroutines.channel

import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

class ConsumerTest : AnnotationSpec() {

    private val testProducer = producer<Int> {
        repeat(5) { send(it, isLastSend = it == 4) }
    }

    @Test
    fun testConsumer() = runBlocking {
        val consumerResult = ArrayList<Int>()
        consumer<Int> { consumerResult.add(it) }.run {
            testProducer.produce().consume().join()
        }
        assertEquals(5, consumerResult.size)
        consumerResult.forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }.asUnit()
}