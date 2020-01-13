package de.gapps.utils.delegates

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.misc.asUnit
import de.gapps.utils.misc.ifN
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlin.reflect.KProperty

open class OnChangedS<P : Any, T>(
    initial: T,
    private val scope: CoroutineScope? = null,
    private val mutex: Mutex? = null,
    onChange: suspend T.(T) -> Unit
) : OnChanged<P, T>(initial, { v -> scope?.launchEx { onChange(v) } }) {

    override fun setValue(thisRef: P, property: KProperty<*>, value: T) = (scope?.launchEx(mutex = mutex) {
        super.setValue(thisRef, property, value)
    } ifN { super.setValue(thisRef, property, value) }).asUnit()
}