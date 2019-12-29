package de.gapps.utils.coroutines.channel

import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.coroutines.scope.IoCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex

interface IProcessingParams {
    val scope: CoroutineScope
    val mutex: Mutex?
    val channelCapacity: Int

}

data class ProcessingParams(
    override val scope: CoroutineScope = DefaultCoroutineScope(),
    override val mutex: Mutex? = null,
    override val channelCapacity: Int = Channel.BUFFERED
) : IProcessingParams

enum class ParallelProcessingTypes {
    UNIQUE,
    EQUAL
}

interface IParallelProcessingParams : IProcessingParams {
    val type: ParallelProcessingTypes
    val numParallel: Int
}

data class ParallelProcessingParams(
    override val type: ParallelProcessingTypes,
    override val numParallel: Int = 8,
    override val scope: CoroutineScope = IoCoroutineScope(),
    override val mutex: Mutex? = null,
    override val channelCapacity: Int = Channel.BUFFERED
) : IParallelProcessingParams