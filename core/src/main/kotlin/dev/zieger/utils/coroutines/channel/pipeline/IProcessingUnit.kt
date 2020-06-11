@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.channel.pipeline.ProcessingElementStage.*
import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
import dev.zieger.utils.log.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

interface IProcessingType {

    val isProducer get() = false
    val isProcessor get() = false
    val isConsumer get() = false
    val isPipeline get() = false
    val isParallelProcessor get() = false
}

interface IProcessingUnit<out I : Any, out O : Any> : IProcessingType, Identity, IProcessingWatchDog {

    var params: IProcessingParams
    val scope: CoroutineScopeEx
    var pipeline: IPipeline<*, *>

    val outputChannel: Channel<out IPipeValue<@UnsafeVariance O>>
    var outIdx: Int

    @Suppress("LeakingThis", "UNCHECKED_CAST")
    fun producerScope(inIdx: Int = 0) =
        ProducerScope<O>(inIdx, outIdx++, params.parallelIdx, params.type, params, scope, { closeOutput() }) {
            tick(SEND_OUTPUT)
            (outputChannel as SendChannel<IPipeValue<O>>).send(it)
        }

    fun processingScope(rawValue: IPipeValue<@UnsafeVariance I>) =
        ProcessingScope(rawValue, producerScope(rawValue.outIdx))

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<O>> =
        scope.launchEx {
            var prevValue: IPipeValue<I>? = null
            tick(RECEIVE_INPUT)
            for (value in this@process) {
                tick(PROCESSING)
                processingScope(value).processSingle(value.value)
                prevValue = value
                tick(RECEIVE_INPUT)
            }
            prevValue?.let { pv -> processingScope(pv).closeOutput() }
        }.let { outputChannel }


    suspend fun IProducerScope<@UnsafeVariance O>.closeOutput() {
        Log.v("processing finished params=$params")
        tick(FINISHED_PROCESSING)
        onProcessingFinished()
        outputChannel.close()
        tick(FINISHED_CLOSING)
    }

    suspend fun IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.processSingle(value: @UnsafeVariance I)

    suspend fun IProducerScope<@UnsafeVariance O>.onProcessingFinished() = Unit
}

abstract class AbsProcessingUnit<out I : Any, out O : Any> : IProcessingUnit<I, O>,
    IProcessingWatchDog by ProcessingWatchDog() {

    override val scope: CoroutineScopeEx by lazy { params.scopeFactory() }
    override var pipeline: IPipeline<*, *> = DummyPipeline()
    override var outIdx: Int = 0
}

fun <I : Any, O : Any> IProcessingUnit<I, O>.withParallelIdx(idx: Int) = apply { params = params.withParallelIdx(idx) }