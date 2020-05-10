@file:Suppress("LeakingThis")

package dev.zieger.utils.delegates

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.asUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


interface IOnChangedBase<P : Any?, out T : Any?, out S : IOnChangedScope2<@UnsafeVariance P, T>> :
    IScope2Factory<P, T, @UnsafeVariance S>, ReadWriteProperty<P, @UnsafeVariance T> {
    val value: @UnsafeVariance T

    val storeRecentValues: Boolean
    val notifyForInitial: Boolean
    val notifyOnChangedValueOnly: Boolean
    val scope: CoroutineScope?
    val mutex: Mutex?

    /**
     * Is invoked before every change of the property. When returning `true` the new value is not assigned
     * to the property.
     */
    fun vetoInternal(value: @UnsafeVariance T): Boolean

    /**
     * Suspend on change callback. Only is invoked when [scope] is set.
     * The [IOnChangedScope2] provides access to the previous values of this property (if [storeRecentValues] is `true`)
     * and the property holding object instance [P].
     */
    suspend fun (@UnsafeVariance S).onChangedSInternal(value: @UnsafeVariance T)

    /**
     * Unsuspended on change callback. Will be called immediately when a new value is set.
     * The [IOnChangedScope2] provides access to the previous values of this property (if [storeRecentValues] is `true`)
     * and the property holding object instance [P].
     */
    fun (@UnsafeVariance S).onChangedInternal(value: @UnsafeVariance T)

    /**
     * Clears the recent value storage.
     */
    fun clearRecentValues()
}

open class OnChangedBase<P : Any?, out T : Any?, out S : IOnChangedScope2<P, T>>(
    initial: T,
    override val storeRecentValues: Boolean,
    notifyForInitial: Boolean,
    override val notifyOnChangedValueOnly: Boolean,
    scope: CoroutineScope?,
    override val mutex: Mutex?,
    scopeFactory: IScope2Factory<P, T, S>,
    open val veto: (@UnsafeVariance T) -> Boolean,
    open val onChangedS: suspend (@UnsafeVariance S).(@UnsafeVariance T) -> Unit,
    open val onChanged: (@UnsafeVariance S).(@UnsafeVariance T) -> Unit
) : IOnChangedBase<P, T, S>, IScope2Factory<P, T, @UnsafeVariance S> by scopeFactory {

    @Suppress("CanBePrimaryConstructorProperty")
    override val notifyForInitial: Boolean = notifyForInitial

    @Suppress("CanBePrimaryConstructorProperty")
    override val scope: CoroutineScope? = scope

    protected var previousThisRef = AtomicReference<P?>(null)
    protected val recentValues = ArrayList<@UnsafeVariance T?>()

    override var value: @UnsafeVariance T = initial
        set(newValue) {
            val block = {
                if (!vetoInternal(newValue) && (field != newValue || !notifyOnChangedValueOnly)) {
                    val old = field
                    field = newValue
                    onPropertyChanged(value, old)
                }
            }
            scope?.launchEx(mutex = mutex) { block() } ?: block()
        }

    init {
        if (notifyForInitial) createScope(
            initial, previousThisRef.get(), null, recentValues,
            { clearRecentValues() }, true
        ).apply {
            onChanged(initial)
            scope?.launchEx(mutex = mutex) { onChangedS(initial) }
        }
    }

    override fun clearRecentValues() = recentValues.clear().asUnit()

    private fun onPropertyChanged(
        new: @UnsafeVariance T,
        old: @UnsafeVariance T?
    ) {
        if (storeRecentValues) recentValues.add(old)
        notifyListener(new, old)
    }

    private fun notifyListener(
        new: @UnsafeVariance T, old: @UnsafeVariance T?,
        isInitialNotification: Boolean = false
    ) = createScope(new, previousThisRef.get(), old, recentValues, { clearRecentValues() }, isInitialNotification)
        .apply {
            onChangedInternal(new)
            scope?.launchEx(mutex = mutex) { onChangedSInternal(new) }
        }

    @Suppress("LeakingThis")
    override fun setValue(thisRef: P, property: KProperty<*>, value: @UnsafeVariance T) {
        previousThisRef.set(thisRef)
        this.value = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): T = value

    override fun vetoInternal(value: @UnsafeVariance T): Boolean = veto(value)

    override suspend fun @UnsafeVariance S.onChangedSInternal(value: @UnsafeVariance T) =
        onChangedS(value)

    override fun @UnsafeVariance S.onChangedInternal(value: @UnsafeVariance T) = onChanged(value)
}
