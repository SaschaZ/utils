@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.coroutines.channel.pipeline

import kotlinx.coroutines.channels.ReceiveChannel


interface IPipelineElement<out I : Any?, out O : Any?> : Identity {

    var params: IProcessingParams
    var pipeline: IPipeline<*, *>

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<IPipeValue<O>>
}

val IPipelineElement<*, *>.scope
    get() = params.scope

fun <I : Any, O : Any> IProcessor<I, O>.withParallelIdx(idx: Int) = apply { params = params.withParallelIdx(idx) }