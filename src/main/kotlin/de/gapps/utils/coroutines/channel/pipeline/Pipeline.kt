package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.IConsumer
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.channel.IProducer
import kotlinx.coroutines.channels.ReceiveChannel

interface IPipeline {

    val producer: IProducer<*>
    val pipes: List<IProcessor<*, *>>
    val consumer: IConsumer<*>

    fun start()
}

class Pipeline(
    override val producer: IProducer<*>,
    override val pipes: List<IProcessor<*, *>>,
    override val consumer: IConsumer<*>
) : IPipeline {

    override fun start() {
        val producerChannel = producer.produce()
        var prevChannel: ReceiveChannel<Any?>? = null
        pipes.map { cur -> prevChannel = cur.run { prevChannel?.process() ?: producerChannel.process() } }
        consumer.run { prevChannel?.process() }
    }
}

