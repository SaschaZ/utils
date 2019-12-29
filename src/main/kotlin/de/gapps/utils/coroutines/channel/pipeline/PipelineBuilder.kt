package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.IConsumer
import de.gapps.utils.coroutines.channel.IProcessor
import de.gapps.utils.coroutines.channel.IProducer
import de.gapps.utils.log.Log

data class PipelineBuilderScope<I>(
    val producer: IProducer<I>,
    val processors: List<IProcessor<*, *>>
) {
    constructor(
        producer: IProducer<I>,
        processor: IProcessor<*, *>
    ) : this(producer, listOf(processor))

//    init {
//        assertTypes(producer.outputType, processors.firstOrNull()?.inputType, 0)
//        if (processors.size > 1) processors.mapPrevIndexed { index, cur, prev -> if (cur != prev) assertTypes(cur.inputType, prev.outputType, index) }
//    }
}

//fun assertTypes(clazz0: KClass<*>, clazz1: KClass<*>?, idx: Int) {
//    if (clazz0 != clazz1) throw IllegalArgumentException("types do not match. $clazz0 and $clazz1 (idx: $idx)")
//}

operator fun <I> IProducer<I>.plus(processor: IProcessor<*, *>) = PipelineBuilderScope(this, processor)

suspend inline operator fun <I, O> IProducer<I>.plus(consumer: IConsumer<O>) =
    Pipeline<I, O>(params, emptyList()).run {
        //        assertTypes(outputType, consumer.inputType, 1)
        pipeline = this
        consumer.pipeline = this
        consumer.run {
            produce().process().consume().run { Log.v("before pipe join"); join(); Log.v("after pipe join") }
        }
    }

operator fun <I> PipelineBuilderScope<I>.plus(processor: IProcessor<*, *>) =
    PipelineBuilderScope(producer, listOf(*this@plus.processors.toTypedArray(), processor))

suspend operator fun <I, O> PipelineBuilderScope<I>.plus(consumer: IConsumer<O>) =
    Pipeline<I, O>(producer.params, processors).run {
        //        assertTypes(processors.last().outputType, consumer.inputType, processors.size)
        producer.pipeline = this
        consumer.pipeline = this
        consumer.run {
            producer.produce().process().consume().join()
        }
    }