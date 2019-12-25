package de.gapps.utils.coroutines.channel.pipeline

import de.gapps.utils.coroutines.channel.IProcessValue
import de.gapps.utils.coroutines.channel.IProcessingParams
import kotlinx.coroutines.channels.ReceiveChannel

interface IPipelineElement<out I, out O> {

    val params: IProcessingParams

    var pipeline: IPipeline<*, *>

    fun ReceiveChannel<IProcessValue<@UnsafeVariance I>>.pipe(): ReceiveChannel<IProcessValue<O>>
}