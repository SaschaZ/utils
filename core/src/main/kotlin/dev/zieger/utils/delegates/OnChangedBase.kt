@file:Suppress("LeakingThis")

package dev.zieger.utils.delegates

import dev.zieger.utils.coroutines.Continuation
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.IDurationEx
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


interface IOnChangedBase<P : Any?, T : Any?> : IOnChangedParams2<P, T>,
    IScope2Factory<P, T>, ReadWriteProperty<P, T> {
    var value: T

    /**
     * When [storeRecentValues] is `true` this [List] contains all changed values since the last
     * [clearRecentValues] invocation.
     */
    val recentValues: FiFo<T?>

    /**
     * Is invoked before every change of the property. When returning `true` the new value is not assigned
     * to the property.
     */
    fun vetoInternal(value: T): Boolean

    /**
     * Suspends until the next change occurs. Will throw a runtime exception if the [timeout] is reached.
     */
    suspend fun nextChange(
        timeout: IDurationEx? = null,
        onChanged: suspend IOnChangedScope2<P, T>.(T) -> Unit = {}
    ): T

    /**
     * Suspends until the observed property changes to [wanted].
     */
    suspend fun suspendUntil(
        wanted: T,
        timeout: IDurationEx? = null
    )

    /**
     * Suspend on change callback. Only is invoked when [scope] is set.
     * The [IOnChangedScope2] provides access to the previous values of this property (if [storeRecentValues] is `true`)
     * and the property holding object instance [P].
     */
    suspend fun IOnChangedScope2<P, T>.onChangedSInternal(value: T)

    /**
     * Unsuspended on change callback. Will be called immediately when a new value is set.
     * The [IOnChangedScope2] provides access to the previous values of this property (if [storeRecentValues] is `true`)
     * and the property holding object instance [P].
     */
    fun IOnChangedScope2<P, T>.onChangedInternal(value: T)

    /**
     * Clears the recent value storage.
     */
    fun clearRecentValues()
}


open class OnChangedBase<P : Any?, T : Any?>(
    params: IOnChangedParams2<P, T>
) : IOnChangedBase<P, T>, IOnChangedParams2<P, T> by params, IScope2Factory<P, T> by params.scopeFactory {

    protected var previousThisRef = AtomicReference<P?>(null)
    override val recentValues = FiFo<T?>(recentValueSize)

    override var value: T = initial
        set(newValue) {
            if (!vetoInternal(newValue) && (field != newValue || !notifyOnChangedValueOnly)) {
                val old = field
                field = newValue
                onPropertyChanged(value, old)
            }
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

    private val nextChangeContinuation = Continuation()

    override suspend fun nextChange(
        timeout: IDurationEx?,
        onChanged: suspend IOnChangedScope2<P, T>.(T) -> Unit
    ): T = withTimeout(timeout) {
        nextChangeContinuation.suspendUntilTrigger(timeout)
        createScope(
            value, previousThisRef.get(), recentValues.lastOrNull(), recentValues, { recentValues.reset() }, false
        ) { value = it }.onChanged(value)
        value
    }

    override suspend fun suspendUntil(
        wanted: T,
        timeout: IDurationEx?
    ) = withTimeout(timeout) {
        if (value == wanted) return@withTimeout

        @Suppress("ControlFlowWithEmptyBody")
        while (nextChange() != wanted);
    }

    override fun clearRecentValues() = recentValues.reset().asUnit()

    private fun onPropertyChanged(
        new: T,
        old: T?
    ) {
        if (recentValueSize > 0) recentValues.put(old)
        notifyListener(new, old)
    }

    private fun notifyListener(
        new: T, old: T?,
        isInitialNotification: Boolean = false
    ) = createScope(new, previousThisRef.get(), old, recentValues, { clearRecentValues() }, isInitialNotification)
        .apply {
            nextChangeContinuation.trigger()
            onChangedInternal(new)
            scope?.launchEx(mutex = mutex) { onChangedSInternal(new) }
        }

    @Suppress("LeakingThis")
    override fun setValue(thisRef: P, property: KProperty<*>, value: T) {
        previousThisRef.set(thisRef)
        this.value = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): T = value

    override fun vetoInternal(value: T): Boolean = veto(value)

    override suspend fun IOnChangedScope2<P, T>.onChangedSInternal(value: T) = onChangedS(value)

    override fun IOnChangedScope2<P, T>.onChangedInternal(value: T) = onChanged(value)
}
