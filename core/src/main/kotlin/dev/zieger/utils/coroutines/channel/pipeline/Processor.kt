@file:Suppress("FunctionName", "unused")

package dev.zieger.utils.coroutines.channel.pipeline

import kotlinx.coroutines.channels.Channel

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

    override val isProcessor: Boolean get() = true

    override suspend fun IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.processSingle(value: @UnsafeVariance I) =
        block(value)
}
