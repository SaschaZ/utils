package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.network.INodeValue
import kotlinx.coroutines.channels.ReceiveChannel

interface IPipelineElement<out I : Any?, out O : Any?> {

    val params: IProcessingParams

    var pipeline: IPipeline<*, *>

    fun ReceiveChannel<INodeValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<INodeValue<O>>
}