package de.gapps.utils.coroutines.channel.pipeline

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.concurrent.ConcurrentHashMap

interface IPipelineStorage {

    fun store(key: String, value: Any)
    fun <T> read(key: String): T
}

interface IPipeline<out I : Any, out O : Any> : IProcessor<I, O>, IPipelineStorage

abstract class AbsPipeline<out I : Any, out O : Any>(
    override val params: IProcessingParams,
    private val processors: List<IProcessor<*, *>>
) : IPipeline<I, O>, Processor<I, O>() {

    private val storage = ConcurrentHashMap<String, Any>()

    override fun store(key: String, value: Any) {
        storage[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> read(key: String) = storage[key] as T

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<O>> {
        var prevChannel: ReceiveChannel<IPipeValue<Any>> = this
        processors.forEach { cur -> prevChannel = cur.run { prevChannel.process() } }
        @Suppress("UNCHECKED_CAST")
        return prevChannel as ReceiveChannel<IPipeValue<O>>
    }
}

class Pipeline<out I : Any, out O : Any>(
    params: IProcessingParams,
    pipes: List<IProcessor<*, *>>
) : AbsPipeline<I, O>(params, pipes) {

    override var pipeline: IPipeline<*, *> = this.apply {
        pipes.forEach { it.pipeline = this }
    }
}

class DummyPipeline : IPipeline<Any, Any> {

    override fun ReceiveChannel<IPipeValue<Any>>.process(): ReceiveChannel<IPipeValue<Any>> = Channel()

    override val params: IProcessingParams = ProcessingParams()
    override var pipeline: IPipeline<*, *> = this

    override fun store(key: String, value: Any) = Unit

    override fun <T> read(key: String): T = throw IllegalStateException("Pipeline is not attached")
}