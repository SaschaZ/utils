@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.coroutines.channel.pipeline

import kotlinx.coroutines.channels.ReceiveChannel


interface IPipelineElement<out I : Any?, out O : Any?> : Identity {

    val params: IProcessingParams
    var pipeline: IPipeline<*, *>

    fun ReceiveChannel<IPipeValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<IPipeValue<O>>
}

val IPipelineElement<*, *>.scope
    get() = params.scope