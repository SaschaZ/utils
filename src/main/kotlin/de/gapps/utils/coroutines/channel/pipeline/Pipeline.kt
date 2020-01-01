package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.*
import de.gapps.utils.coroutines.channel.network.INodeValue
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.concurrent.ConcurrentHashMap

interface IPipelineStorage {

    fun store(key: String, value: Any)
    fun <T> read(key: String): T
}

interface IPipeline<I, O> : IProcessor<I, O>, IPipelineStorage

abstract class AbsPipeline<I, O>(
    override val params: IProcessingParams,
    private val processors: List<IProcessor<*, *>>
) : IPipeline<I, O>, Processor<I, O>() {

    private val storage = ConcurrentHashMap<String, Any>()

    init {
        @Suppress("LeakingThis")
        block = {

        }
    }

    override fun store(key: String, value: Any) {
        storage[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> read(key: String) = storage[key] as T

    override fun ReceiveChannel<INodeValue<I>>.process(): ReceiveChannel<INodeValue<O>> {
        var prevChannel: ReceiveChannel<INodeValue<Any?>> = this
//        processors.map { cur -> prevChannel = (cur as ReceiveChannel<IProcessValue<Any?>>).run { prevChannel.process() } }
        @Suppress("UNCHECKED_CAST")
        return prevChannel as ReceiveChannel<INodeValue<O>>
    }
}

class Pipeline<I, O>(
    params: IProcessingParams,
    pipes: List<IProcessor<*, *>>
) : AbsPipeline<I, O>(params, pipes) {

    override var pipeline: IPipeline<*, *> = this.apply {
        pipes.forEach { it.pipeline = this }
    }
}

class DummyPipeline : IPipeline<Any, Any> {

    override fun ReceiveChannel<INodeValue<Any>>.process(): ReceiveChannel<INodeValue<Any>> = Channel()

    override val params: IProcessingParams =
        ProcessingParams()
    override var pipeline: IPipeline<*, *> = this
    override var onProcessingFinished: suspend IProcessingScope<Any>.() -> Unit = {}

    override fun store(key: String, value: Any) = Unit

    override fun <T> read(key: String): T = throw IllegalStateException("Pipeline is not attached")
}
