package dev.zieger.utils.time

import dev.zieger.utils.misc.BaseFiFo
import dev.zieger.utils.misc.whenNotNull
import dev.zieger.utils.time.base.IDurationEx
import dev.zieger.utils.time.base.ITimeEx

open class DurationFiFo<T : ITimeEx>(
    private val maxDuration: IDurationEx,
    initial: List<T> = emptyList()
) : BaseFiFo<T>(initial, shouldRemove = { min()?.let { f -> (it - f) > maxDuration } ?: false }) {
    override val isFull: Boolean
        get() = whenNotNull(internal.firstOrNull(), internal.lastOrNull()) { f, l ->
            (l - f) > maxDuration
        } ?: false
}