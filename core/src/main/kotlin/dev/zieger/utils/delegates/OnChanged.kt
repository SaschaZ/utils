@file:Suppress("LeakingThis", "unused")

package dev.zieger.utils.delegates

import dev.zieger.utils.coroutines.Continuation
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.delegates.OnChangedParamsWithParent.Companion.DEFAULT_RECENT_VALUE_BUFFER_SIZE
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.ifNull
import dev.zieger.utils.misc.lastOrNull
import dev.zieger.utils.time.duration.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.lang.ref.WeakReference
import java.util.*
import kotlin.reflect.KProperty

/**
 *
 */
typealias OnChanged<T> = OnChangedWithParent<Any?, T>

/**
 * Implementation of [IOnChangedWithParent].
 */
open class OnChangedWithParent<P : Any?, T : Any?>(
    params: IOnChangedParamsWithParent<P, T>
) : IOnChangedWithParent<P, T>, IOnChangedParamsWithParent<P, T> by params {

    /**
     * @param initial The observed property will be initialized with this value.
     * @param storeRecentValues If set to `true` all values of the property will be stored and provided within the
     * [IOnChangedScopeWithParent]. Should be set to `false` when the values of the property consume too much memory.
     * Defaulting is `false`.
     * @param recentValueSize Size of the fifo used to store the recent values. Will be set to
     * [DEFAULT_RECENT_VALUE_BUFFER_SIZE] if [storeRecentValues] is `true`.
     * @param notifyForInitial When `true` a new listener will immediately notified for the initial value of the
     * property without the need of a change. Default is `false`.
     * @param notifyOnChangedValueOnly When `false` listener are only notified when the value of the property changed.
     * When `true` every "set" to the property will notify the listener. Default is `true`.
     * @param scope [CoroutineScope] that is used to notify listener that requested to be notified within a coroutine.
     * Default is `null`.
     * @param mutex If not `null` the [Mutex] will wrap the whole execution of [scope]. Default is `null`.
     * @param veto Is invoked before every change of the property. When returning `true` the new value is not assigned
     * to the property. (Optional)
     * @param map Maps the new input value to the internal property. Is called after `veto` and before `onChanged`.
     * @param onChangedS Suspend on change callback. Only is invoked when [scope] is set. (Optional)
     * @param onChanged Unsuspended on change callback. Will be called immediately when a new value is set. (Optional)
     */
    constructor(
        initial: T,
        scope: CoroutineScope? = null,
        storeRecentValues: Boolean = false,
        recentValueSize: Int = if (storeRecentValues) DEFAULT_RECENT_VALUE_BUFFER_SIZE else 0,
        notifyForInitial: Boolean = false,
        notifyOnChangedValueOnly: Boolean = true,
        mutex: Mutex = Mutex(),
        veto: (T) -> Boolean = { false },
        map: (T) -> T = { it },
        onChangedS: (suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit)? = null,
        onChanged: (IOnChangedScopeWithParent<P, T>.(T) -> Unit)? = null
    ) : this(
        OnChangedParamsWithParent(
            initial, scope, storeRecentValues, recentValueSize, notifyForInitial, notifyOnChangedValueOnly,
            mutex, veto, map, onChangedS, onChanged
        )
    )

    protected open var parent: WeakReference<P>? = null
    open var propertyName: String = ""
        protected set
    override val previousValues = FiFo<T?>(previousValueSize)
    protected open var previousValuesCleared: Boolean = false
    protected open val nextChangeContinuation = Continuation()
    protected open val valueWaiter = LinkedList<Pair<T?, Continuation>>()

    protected open var internalValue: T = initial
        set(newValue) {
            if (vetoInternal(newValue)) return
            val mappedInput = mapInternal(newValue)
            if (field != mappedInput || !notifyOnChangedValueOnly) {
                val old = field
                field = mappedInput
                onPropertyChanged(value, old)
            }
        }

    override var value: T
        get() = internalValue
        set(newValue) {
            scope?.launch { changeValue { newValue } } ifNull { internalValue = newValue }
        }

    init {
        if (onChangedS != null) require(scope != null) { "When using `onChangedS`, `scope` can not be `null`." }

        if (notifyForInitial) buildOnChangedScope(null, true).apply {
            onChangedInternal(initial)
            scope?.launchEx(mutex = mutex) { onChangedSInternal(initial) }
        }
    }

    override suspend fun changeValue(block: suspend IOnChangedScopeWithParent<P, T>.(T) -> T): Unit = mutex.withLock {
        internalValue = block(buildOnChangedScope(previousValues.lastOrNull()), internalValue)
    }.asUnit()

    override suspend fun nextChange(
        timeout: IDurationEx?,
        onChanged: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit
    ): T {
        val old = value
        nextChangeContinuation.suspend(timeout)
        buildOnChangedScope(old).onChanged(value)
        return value
    }

    override suspend fun suspendUntil(
        wanted: T,
        timeout: IDurationEx?,
        onChanged: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit
    ) {
        val cont = Continuation()
        withTimeout(timeout, {
            valueWaiter.remove(wanted to cont)
            "Timeout when waiting for $wanted. Is $value and was [${previousValues.joinToString(", ")}]."
        }) {
            if (value == wanted) return@withTimeout

            valueWaiter.add(wanted to cont)
            cont.suspend()
        }
    }

    protected open fun buildOnChangedScope(
        previousValue: T?,
        isInitialNotification: Boolean = false
    ): OnChangedScopeWithParent<P, T> =
        OnChangedScopeWithParent(
            if (isInitialNotification) initial else value, parent?.get(), propertyName, previousValue,
            previousValues, { previousValues.clear() }, isInitialNotification
        ) { value = it }

    override fun clearPreviousValues() {
        previousValues.clear()
        previousValuesCleared = true
    }

    protected open fun onPropertyChanged(
        new: T,
        old: T
    ) {
        if (!previousValuesCleared && previousValueSize > 0)
            previousValues.put(old)
        previousValuesCleared = false

        buildOnChangedScope(old).apply {
            onChangedInternal(new)
            scope?.launchEx(mutex = mutex) { onChangedSInternal(new) }
        }

        nextChangeContinuation.resume()
        valueWaiter.removeAll { (wanted, cont) ->
            if (new == wanted) {
                cont.resume()
                true
            } else false
        }
    }

    @Suppress("LeakingThis")
    override fun setValue(thisRef: P, property: KProperty<*>, value: T) {
        parent = WeakReference(thisRef)
        propertyName = property.name
        this.value = value
    }

    override fun getValue(thisRef: P, property: KProperty<*>): T {
        parent = WeakReference(thisRef)
        propertyName = property.name
        return value
    }

    override fun vetoInternal(value: T): Boolean = veto(value)

    override fun mapInternal(value: T): T = map(value)

    override suspend fun IOnChangedScopeWithParent<P, T>.onChangedSInternal(value: T) =
        onChangedS?.invoke(this, value).asUnit()

    override fun IOnChangedScopeWithParent<P, T>.onChangedInternal(value: T) =
        onChanged?.invoke(this, value).asUnit()
}