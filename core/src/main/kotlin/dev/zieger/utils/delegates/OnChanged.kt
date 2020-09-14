@file:Suppress("LeakingThis", "unused")

package dev.zieger.utils.delegates

import dev.zieger.utils.coroutines.Continuation
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.delegates.OnChangedParamsWithParent.Companion.DEFAULT_RECENT_VALUE_BUFFER_SIZE
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.base.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicReference
import kotlin.reflect.KProperty


typealias OnChanged<T> = OnChangedWithParent<Any?, T>

/**
 * Implementation of [IOnChangedWithParent] using [IOnChangedParamsWithParent] for initialization.
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
        storeRecentValues: Boolean = false,
        recentValueSize: Int = if (storeRecentValues) DEFAULT_RECENT_VALUE_BUFFER_SIZE else 0,
        notifyForInitial: Boolean = false,
        notifyOnChangedValueOnly: Boolean = true,
        scope: CoroutineScope? = null,
        mutex: Mutex? = null,
        safeSet: Boolean = false,
        veto: (T) -> Boolean = { false },
        map: (T) -> T = { it },
        onChangedS: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit = {},
        onChanged: IOnChangedScopeWithParent<P, T>.(T) -> Unit = {}
    ) : this(
        OnChangedParamsWithParent(
            initial, storeRecentValues, recentValueSize, notifyForInitial, notifyOnChangedValueOnly,
            scope, mutex, safeSet, veto, map, onChangedS, onChanged
        )
    )

    protected var previousThisRef = AtomicReference<P?>(null)
    override val previousValues = FiFo<T?>(previousValueSize)

    override var value: T = initial
        set(newValue) {
            fun internalSet() {
                val vetoActive = vetoInternal(newValue)
                if (vetoActive) return
                val mappedInput = mapInternal(newValue)
                if (field != mappedInput || !notifyOnChangedValueOnly) {
                    val old = field
                    field = mappedInput
                    onPropertyChanged(value, old)
                }
            }
            if (safeSet) scope?.launchEx(mutex = mutex) { internalSet() } else internalSet()
        }

    init {
        if (notifyForInitial) OnChangedScopeWithParent(
            initial, previousThisRef.get(), null, previousValues,
            { clearPreviousValues() }, true
        ).apply {
            onChanged(initial)
            scope?.launchEx(mutex = mutex) { onChangedS(initial) }
        }
    }

    override suspend fun changeValue(block: (T) -> T): Unit = mutex?.withLock {
        value = block(value)
    }.asUnit()

    private val nextChangeContinuation = Continuation()

    override suspend fun nextChange(
        timeout: IDurationEx?,
        onChanged: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit
    ): T = withTimeout(timeout) {
        nextChangeContinuation.suspendUntilTrigger(timeout)
        OnChangedScopeWithParent(
            value, previousThisRef.get(), previousValues.lastOrNull(), previousValues, { previousValues.clear() }, false
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

    override fun clearPreviousValues() = previousValues.clear().asUnit()

    private fun onPropertyChanged(
        new: T,
        old: T?
    ) {
        if (previousValueSize > 0) previousValues.put(old)
        notifyListener(new, old)
    }

    private fun notifyListener(
        new: T, old: T?,
        isInitialNotification: Boolean = false
    ) = OnChangedScopeWithParent(
        new,
        previousThisRef.get(),
        old,
        previousValues,
        { clearPreviousValues() },
        isInitialNotification
    ).apply {
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

    override fun mapInternal(value: T): T = map(value)

    override suspend fun IOnChangedScopeWithParent<P, T>.onChangedSInternal(value: T) = onChangedS(value)

    override fun IOnChangedScopeWithParent<P, T>.onChangedInternal(value: T) = onChanged(value)
}
