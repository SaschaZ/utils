package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.core_testing.assertion.assert
import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

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
        finished assert true
        consumerResult.size assert 5
        consumerResult.forEachIndexed { index, i ->
            index assert i
        }
    }.asUnit()
}