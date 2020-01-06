package de.gapps.utils.coroutines.channel.network

import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import kotlinx.coroutines.sync.Mutex

@Suppress("ClassName")
sealed class SProcessingType {

    internal abstract val parallelUnits: Int
    internal abstract val scope: CoroutineScopeEx
    internal abstract val mutex: Mutex?

    class PARALLEL_EQUAL(
        override val parallelUnits: Int,
        override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
        override val mutex: Mutex? = null
    ) : SProcessingType()

    class PARALLEL_UNIQUE(
        override val parallelUnits: Int,
        override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
        override val mutex: Mutex? = null
    ) : SProcessingType()

    class SEQUENTIAL(
        override val scope: CoroutineScopeEx = DefaultCoroutineScope(),
        override val mutex: Mutex? = null
    ) : SProcessingType() {
        override val parallelUnits: Int = 1
    }
}