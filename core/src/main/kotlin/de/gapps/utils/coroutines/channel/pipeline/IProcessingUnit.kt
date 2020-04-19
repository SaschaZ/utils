@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.builder.launchEx
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.reflect.KClass


interface IProcessingUnit<out I : Any, out O : Any> : Identity {

    var params: IProcessingParams
    var pipeline: IPipeline<*, *>

    val inputType: KClass<out @UnsafeVariance I>
    val outputType: KClass<out @UnsafeVariance O>

    val outputChannel: Channel<out IPipeValue<@UnsafeVariance O>>
    var outIdx: Int

    @Suppress("LeakingThis", "UNCHECKED_CAST")
    fun producerScope(inIdx: Int = 0) =
        ProducerScope<O>(inIdx, outIdx++, params.parallelIdx, params.type, params, { closeOutput() }) {
            pipeline.tick(this, PipelineElementStage.SEND_OUTPUT)
            (outputChannel as SendChannel<IPipeValue<O>>).send(it)
        }

    fun processingScope(rawValue: IPipeValue<@UnsafeVariance I>) =
        ProcessingScope(rawValue, producerScope(rawValue.outIdx))

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.process(): ReceiveChannel<IPipeValue<O>> =
        scope.launchEx {
            var prevValue: IPipeValue<I>? = null
            pipeline.tick(this@IProcessingUnit, PipelineElementStage.RECEIVE_INPUT)
            for (value in this@process) {
                pipeline.tick(this@IProcessingUnit, PipelineElementStage.PROCESSING)
                processingScope(value).processSingle(value.value)
                prevValue = value
                pipeline.tick(this@IProcessingUnit, PipelineElementStage.RECEIVE_INPUT)
            }
            prevValue?.let { pv -> processingScope(pv).closeOutput() }
        }.let { outputChannel }


    suspend fun IProducerScope<@UnsafeVariance O>.closeOutput() {
        Log.v("processing finished params=$params")
        pipeline.tick(this@IProcessingUnit, PipelineElementStage.FINISHED_PROCESSING)
        onProcessingFinished()
        outputChannel.close()
        pipeline.tick(this@IProcessingUnit, PipelineElementStage.FINISHED_CLOSING)
    }

    suspend fun IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.processSingle(value: @UnsafeVariance I)

    suspend fun IProducerScope<@UnsafeVariance O>.onProcessingFinished() = Unit
}

abstract class AbsProcessingUnit<out I : Any, out O : Any>(
    override val inputType: KClass<out @UnsafeVariance I>,
    override val outputType: KClass<out @UnsafeVariance O>
) : IProcessingUnit<I, O> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()
    override var outIdx: Int = 0

    init {
        @Suppress("LeakingThis")
        if (inputType.typeParameters.isNotEmpty() ||
            outputType.typeParameters.isNotEmpty()
        )
            throw IllegalArgumentException("Input and output types must have no type parameters.")
    }
}

val IProcessingUnit<*, *>.scope
    get() = params.scope

fun <I : Any, O : Any> IProcessingUnit<I, O>.withParallelIdx(idx: Int) = apply { params = params.withParallelIdx(idx) }