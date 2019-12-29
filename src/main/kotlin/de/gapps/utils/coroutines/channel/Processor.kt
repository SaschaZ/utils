package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.channel.pipeline.DummyPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipeline
import de.gapps.utils.coroutines.channel.pipeline.IPipelineElement
import de.gapps.utils.coroutines.channel.pipeline.IPipelineStorage
import de.gapps.utils.time.ITimeEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.reflect.KClass

interface IProcessor<out I : Any, out O : Any> :
    IPipelineElement<I, O> {

    val inputType: KClass<@UnsafeVariance I>
    val outputType: KClass<@UnsafeVariance O>

    override fun ReceiveChannel<IProcessValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<IProcessValue<O>> = process()

    fun ReceiveChannel<IProcessValue<@UnsafeVariance I>>.process(): ReceiveChannel<IProcessValue<O>>

    fun onProcessingFinished() = Unit
}

open class Processor<out I : Any, out O : Any>(
    override val params: IProcessingParams = ProcessingParams(),
    override val inputType: KClass<@UnsafeVariance I>,
    override val outputType: KClass<@UnsafeVariance O>,
    protected open var block: (suspend IProcessingScope<@UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit)? = null
) : IProcessor<I, O> {

    override var pipeline: IPipeline<*, *> = DummyPipeline()

    @Suppress("LeakingThis")
    protected val scope: CoroutineScope = params.scope

    @Suppress("LeakingThis")
    private val output = Channel<IProcessValue<O>>(params.channelCapacity)

    override fun ReceiveChannel<IProcessValue<@UnsafeVariance I>>.process() = scope.launchEx {
        for (value in this@process) {
            val b = block ?: throw IllegalStateException("Can not process without callback")
            ProcessingScope<O>(
                value,
                pipeline
            ) { result, time, idx ->
                output.send(ProcessValue(value.outIdx, idx, result, time))
            }.b(value.value)
        }
        output.close()
        onProcessingFinished()
    }.let { output as ReceiveChannel<IProcessValue<O>> }
}

interface IProcessingScope<out O> {

    val inIdx: Int
    var outIdx: Int
    val time: ITimeEx
    val storage: IPipelineStorage

    suspend fun send(
        value: @UnsafeVariance O,
        time: ITimeEx = this.time,
        outIdx: Int = this.outIdx++
    )
}

open class ProcessingScope<out O>(
    inputValue: IProcessValue<*>,
    override val storage: IPipelineStorage,
    private val sender: suspend (
        value: @UnsafeVariance O,
        time: ITimeEx,
        outIdx: Int
    ) -> Unit
) : IProcessingScope<O> {

    override val inIdx: Int = inputValue.outIdx
    override var outIdx: Int = 0
    override val time: ITimeEx = inputValue.time

    override suspend fun send(
        value: @UnsafeVariance O,
        time: ITimeEx,
        outIdx: Int
    ) = sender(value, time, outIdx)
}

inline fun <reified I : Any, reified O : Any> processor(
    params: IProcessingParams = ProcessingParams(),
    noinline block: suspend IProcessingScope<@UnsafeVariance O>.(value: @UnsafeVariance I) -> Unit
) = Processor(params, I::class, O::class, block)