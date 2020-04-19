package de.gapps.utils.coroutines.channel.pipeline

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.reflect.KClass

interface IPipeline<out I : Any, out O : Any> : IProcessingUnit<I, O>, IPipelineWatchDog {
    override suspend fun IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.processSingle(value: @UnsafeVariance I) =
        Unit
}

abstract class AbsPipeline<out I : Any, out O : Any>(
    override var params: IProcessingParams,
    override var outputChannel: Channel<out IPipeValue<@UnsafeVariance O>>,
    inputType: KClass<I>,
    outputType: KClass<O>,
    private val processors: List<IProcessingUnit<*, *>>
) : AbsProcessingUnit<I, O>(inputType, outputType), IPipeline<I, O>, IProcessingUnit<I, O> {

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<O>> {
        var prevChannel: ReceiveChannel<IPipeValue<Any>> = this
        processors.forEach { cur -> prevChannel = cur.run { prevChannel.process() } }
        @Suppress("UNCHECKED_CAST")
        return prevChannel as ReceiveChannel<IPipeValue<O>>
    }
}

open class Pipeline<out I : Any, out O : Any>(
    params: IProcessingParams = ProcessingParams(),
    identity: Identity = Id("Pipeline"),
    outputChannel: Channel<out IPipeValue<O>> = Channel(params.channelCapacity),
    inputType: KClass<I>,
    outputType: KClass<O>,
    processors: List<IProcessingUnit<*, *>>
) : AbsPipeline<I, O>(params, outputChannel, inputType, outputType, processors),
    IPipelineWatchDog by PipelineWatchDog(params.scope),
    Identity by identity {


    override var pipeline: IPipeline<*, *> = apply {
        processors.forEach { it.pipeline = this }
    }
}

class DummyPipeline :
    IPipeline<Any, Any>, IPipelineWatchDog by PipelineWatchDog(), Identity by NoId {

    override fun ReceiveChannel<IPipeValue<Any>>.process(): ReceiveChannel<IPipeValue<Any>> = Channel()

    override var params: IProcessingParams = ProcessingParams()
    override val inputType: KClass<Any> = Any::class
    override val outputType: KClass<Any> = Any::class
    override var outIdx: Int = 0
    override var outputChannel: Channel<out IPipeValue<Any>> = Channel()
    override var pipeline: IPipeline<*, *> = this
}