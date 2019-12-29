package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.IProcessValue
import de.gapps.utils.coroutines.channel.IProcessingParams
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.channel.ProcessingParams
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface IPipelineStorage {

    fun store(key: String, value: Any)
    fun <T> read(key: String): T
}

interface IPipeline<out I : Any, out O : Any> : IProcessor<I, O>, IPipelineStorage

abstract class AbsPipeline<out I : Any, out O : Any>(
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

class Pipeline<out I : Any, out O : Any>(
    params: IProcessingParams,
    override val inputType: KClass<@UnsafeVariance I>,
    override val outputType: KClass<@UnsafeVariance O>,
    pipes: List<IPipelineElement<*, *>>
) : AbsPipeline<I, O>(params, pipes) {

    override var pipeline: IPipeline<*, *> = this.apply {
        pipes.forEach { it.pipeline = this }
    }
}

inline fun <reified I : Any, reified O : Any> pipeline(
    params: IProcessingParams,
    pipes: List<IPipelineElement<*, *>>
) = Pipeline(params, I::class, O::class, pipes)

class DummyPipeline : IPipeline<Any, Any> {

    override val inputType: KClass<Any> = Any::class
    override val outputType: KClass<Any> = Any::class

    override fun ReceiveChannel<IProcessValue<Any>>.process(): ReceiveChannel<IProcessValue<Any>> = Channel()

    override val params: IProcessingParams = ProcessingParams()
    override var pipeline: IPipeline<*, *> = this

    override fun store(key: String, value: Any) = Unit

    override fun <T> read(key: String): T = throw IllegalStateException("Pipeline is not attached")
}
