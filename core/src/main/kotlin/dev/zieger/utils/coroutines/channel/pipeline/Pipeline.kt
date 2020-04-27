package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface IPipeline<out I : Any, out O : Any> : IProcessor<I, O> {
    override suspend fun IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.processSingle(value: @UnsafeVariance I) =
        Unit
}

abstract class AbsPipeline<out I : Any, out O : Any>(
    override var params: IProcessingParams,
    override var outputChannel: Channel<out IPipeValue<@UnsafeVariance O>>,
    private val processors: List<IProcessor<*, *>>
) : AbsProcessingUnit<I, O>(), IPipeline<I, O>, IProcessor<I, O> {

    override fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<O>> {
        var prevChannel: ReceiveChannel<IPipeValue<Any>> = this
        processors.forEach { cur -> cur.pipeline = this@AbsPipeline; prevChannel = cur.run { prevChannel.process() } }
        @Suppress("UNCHECKED_CAST")
        return prevChannel as ReceiveChannel<IPipeValue<O>>
    }
}

open class Pipeline<out I : Any, out O : Any>(
    params: IProcessingParams = ProcessingParams(),
    identity: Identity = Id("Pipeline"),
    outputChannel: Channel<out IPipeValue<O>> = Channel(params.channelCapacity),
    processors: List<IProcessor<*, *>>,
    observingActive: Boolean = false
) : AbsPipeline<I, O>(params, outputChannel, processors),
    Identity by identity {

    @Suppress("LeakingThis")
    override var pipeline: IPipeline<*, *> = this
}

class DummyPipeline :
    IProcessingWatchDog by ProcessingWatchDog(),
    IPipeline<Any, Any>, Identity by NoId {

    override fun ReceiveChannel<IPipeValue<Any>>.process(): ReceiveChannel<IPipeValue<Any>> = Channel()

    override var params: IProcessingParams = ProcessingParams()
    override var outIdx: Int = 0
    override var outputChannel: Channel<out IPipeValue<Any>> = Channel()
    override var pipeline: IPipeline<*, *> = this
    override val scope: CoroutineScopeEx = DefaultCoroutineScope()
}