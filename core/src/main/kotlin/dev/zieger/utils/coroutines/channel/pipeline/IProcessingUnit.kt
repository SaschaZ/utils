@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.coroutines.channel.pipeline

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel


interface IProcessingUnit<out I : Any, out O : Any> : Identity, IProcessingWatchDog {

    var params: IProcessingParams
    val scope: CoroutineScopeEx
    var pipeline: IPipeline<*, *>

    val outputChannel: Channel<out IPipeValue<@UnsafeVariance O>>
    var outIdx: Int

    @Suppress("LeakingThis", "UNCHECKED_CAST")
    fun producerScope(inIdx: Int = 0) =
        ProducerScope<O>(inIdx, outIdx++, params.parallelIdx, params.type, params, scope, { closeOutput() }) {
            tick(this, ProcessingElementStage.SEND_OUTPUT)
            (outputChannel as SendChannel<IPipeValue<O>>).send(it)
        }

    fun processingScope(rawValue: IPipeValue<@UnsafeVariance I>) =
        ProcessingScope(rawValue, producerScope(rawValue.outIdx))

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<O>> =
        scope.launchEx {
            var prevValue: IPipeValue<I>? = null
            tick(this@IProcessingUnit, ProcessingElementStage.RECEIVE_INPUT)
            for (value in this@process) {
                tick(this@IProcessingUnit, ProcessingElementStage.PROCESSING)
                processingScope(value).processSingle(value.value)
                prevValue = value
                tick(this@IProcessingUnit, ProcessingElementStage.RECEIVE_INPUT)
            }
            prevValue?.let { pv -> processingScope(pv).closeOutput() }
        }.let { outputChannel }


    suspend fun IProducerScope<@UnsafeVariance O>.closeOutput() {
        Log.v("processing finished params=$params")
        tick(this@IProcessingUnit, ProcessingElementStage.FINISHED_PROCESSING)
        onProcessingFinished()
        outputChannel.close()
        tick(this@IProcessingUnit, ProcessingElementStage.FINISHED_CLOSING)
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