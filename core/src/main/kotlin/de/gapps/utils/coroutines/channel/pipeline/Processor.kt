@file:Suppress("FunctionName")

package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.builder.launchEx
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface IProcessor<out I : Any, out O : Any> : IProcessingUnit<I, O>

fun <I : Any, O : Any> processor(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("Processor"),
    block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): IProcessor<I, O> = Processor(params, outputChannel, identity, block)

fun <I : Any, O : Any> IParamsHolder.processor(
    outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("Processor"),
    block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): IProcessor<I, O> = Processor(params, outputChannel, identity, block)

open class Processor<out I : Any, out O : Any>(
    override var params: IProcessingParams = ProcessingParams(),
    override var outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("Processor"),
    open val block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit =
        throw IllegalArgumentException("Block can not be empty.")
) : AbsProcessingUnit<I, O>(),
    IProcessor<I, O>,
    Identity by identity {

    override suspend fun IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.processSingle(value: @UnsafeVariance I) =
        block(value)
}

fun <I : Any, O : Any> listProcessor(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<List<@UnsafeVariance O>>> = Channel(params.channelCapacity),
    identity: Identity = Id("ListProcessor"),
    block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): IProcessor<List<I>, List<O>> = ListProcessor(params, outputChannel, identity, block)

fun <I : Any, O : Any> IParamsHolder.listProcessor(
    outputChannel: Channel<IPipeValue<List<@UnsafeVariance O>>> = Channel(params.channelCapacity),
    identity: Identity = Id("ListProcessor"),
    block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): IProcessor<List<I>, List<O>> = ListProcessor(params, outputChannel, identity, block)

open class ListProcessor<out I : Any, out O : Any>(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<List<@UnsafeVariance O>>> = Channel(params.channelCapacity),
    identity: Identity = Id("ListProcessor"),
    private val block2: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit =
        throw IllegalArgumentException("Block can not be empty.")
) : Processor<List<I>, List<O>>(params, outputChannel, identity) {

    override fun ReceiveChannel<IPipeValue<List<@UnsafeVariance I>>>.process(): ReceiveChannel<IPipeValue<List<O>>> =
        outputChannel.also { output ->
            scope.launchEx {
                for (value in this@process) {
                    val result = ArrayList<O>()
                    value.value.forEach { v ->
                        ProcessingScope<I, O>(
                            PipeValue(v, value.time, value.outIdx, outIdx, value.parallelIdx, value.parallelType),
                            ProducerScope(value.parallelIdx, value.outIdx, outIdx, value.parallelType, params, scope,
                                { close() }, { r -> result.add(r.value) })
                        ).block2(v)
                    }
                    output.send(
                        PipeValue(result, value.time, value.outIdx, outIdx++, value.parallelIdx, value.parallelType)
                    )
                }
                output.close()
            }
        }
}

