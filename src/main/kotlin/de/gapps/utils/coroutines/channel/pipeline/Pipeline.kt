package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.IProcessValue
import de.gapps.utils.coroutines.channel.IProcessingParams
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.channel.ProcessingParams
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.concurrent.ConcurrentHashMap

interface IPipelineStorage {

    fun store(key: String, value: Any)
    fun <T> read(key: String): T
}

interface IPipeline<out I, out O> : IProcessor<I, O>, IPipelineStorage

abstract class AbsPipeline<out I, out O>(
    override val params: IProcessingParams,
    private val pipes: List<IPipelineElement<*, *>>
) : IPipeline<I, O> {

    private val storage = ConcurrentHashMap<String, Any>()

    override fun store(key: String, value: Any) {
        storage[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> read(key: String) = storage[key] as T

    override fun ReceiveChannel<IProcessValue<@UnsafeVariance I>>.process(): ReceiveChannel<IProcessValue<@UnsafeVariance O>> {
        var prevChannel: ReceiveChannel<IProcessValue<Any?>> = this
        pipes.map { cur -> prevChannel = cur.run { prevChannel.pipe() } }
        @Suppress("UNCHECKED_CAST")
        return prevChannel as ReceiveChannel<IProcessValue<O>>
    }
}

class Pipeline<out I, out O>(
    params: IProcessingParams,
    pipes: List<IPipelineElement<*, *>>
) : AbsPipeline<I, O>(params, pipes) {

    override var pipeline: IPipeline<*, *> = this.apply {
        pipes.forEach { it.pipeline = this }
    }
}

class DummyPipeline : IPipeline<Any, Any> {

    override fun ReceiveChannel<IProcessValue<Any>>.process(): ReceiveChannel<IProcessValue<Any>> = Channel()

    override val params: IProcessingParams = ProcessingParams()
    override var pipeline: IPipeline<*, *> = this

    override fun store(key: String, value: Any) = Unit

    override fun <T> read(key: String): T = throw IllegalStateException("Pipeline is not attached")
}
