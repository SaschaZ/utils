@file:Suppress("FunctionName")

package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.builder.launchEx
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.reflect.KClass

inline fun <reified I : Any, reified O : Any> processor(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("Processor"),
    noinline block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): IProcessingUnit<I, O> = Processor(params, outputChannel, identity, I::class, O::class, block)

inline fun <reified I : Any, reified O : Any> IParamsHolder.processor(
    outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("Processor"),
    noinline block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): IProcessingUnit<I, O> = Processor(params, outputChannel, identity, I::class, O::class, block)

open class Processor<out I : Any, out O : Any>(
    override var params: IProcessingParams = ProcessingParams(),
    override var outputChannel: Channel<IPipeValue<@UnsafeVariance O>> = Channel(params.channelCapacity),
    identity: Identity = Id("Processor"),
    inputType: KClass<out I>,
    outputType: KClass<out O>,
    open val block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
) : AbsProcessingUnit<I, O>(inputType, outputType),
    Identity by identity {

    override suspend fun IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.processSingle(value: @UnsafeVariance I) =
        block(value)
}

inline fun <reified I : Any, reified O : Any> listProcessor(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<List<@UnsafeVariance O>>> = Channel(params.channelCapacity),
    identity: Identity = Id("ListProcessor"),
    noinline block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): IProcessingUnit<List<I>, List<O>> = ListProcessor(
    params, outputChannel, identity,
    listOf<I>()::class, listOf<O>()::class, block
)

inline fun <reified I : Any, reified O : Any> IParamsHolder.listProcessor(
    outputChannel: Channel<IPipeValue<List<@UnsafeVariance O>>> = Channel(params.channelCapacity),
    identity: Identity = Id("ListProcessor"),
    noinline block: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
): IProcessingUnit<List<I>, List<O>> = ListProcessor(
    params, outputChannel, identity,
    listOf<I>()::class, listOf<O>()::class, block
)

open class ListProcessor<out I : Any, out O : Any>(
    params: IProcessingParams = ProcessingParams(),
    outputChannel: Channel<IPipeValue<List<@UnsafeVariance O>>> = Channel(params.channelCapacity),
    identity: Identity = Id("ListProcessor"),
    inputType: KClass<out List<I>>,
    outputType: KClass<out List<O>>,
    private val block2: suspend IProcessingScope<@UnsafeVariance I, @UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
) : Processor<List<I>, List<O>>(
    params, outputChannel, identity, inputType, outputType, {}
) {

    override fun ReceiveChannel<IPipeValue<List<@UnsafeVariance I>>>.process(): ReceiveChannel<IPipeValue<List<O>>> =
        outputChannel.also { output ->
            scope.launchEx {
                for (value in this@process) {
                    val result = ArrayList<O>()
                    value.value.forEach { v ->
                        ProcessingScope<I, O>(
                            PipeValue(v, value.time, value.outIdx, outIdx, value.parallelIdx, value.parallelType),
                            ProducerScope(value.parallelIdx, value.outIdx, outIdx, value.parallelType, params,
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

