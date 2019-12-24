package de.gapps.utils.coroutines.channel

import de.gapps.utils.misc.asUnit
import io.kotlintest.specs.AnnotationSpec
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.assertEquals

class ProducerTest : AnnotationSpec() {

    @Test
    fun testProducer() = runBlocking {
        val producer = producer<Int> {
            repeat(5) { send(it) }
            close()
        }
        val producerResult = producer.produce().toList()

        assertEquals(5, producerResult.size)
        producerResult.forEachIndexed { index, i ->
            assertEquals(index, i)
        }
    }.asUnit()
}