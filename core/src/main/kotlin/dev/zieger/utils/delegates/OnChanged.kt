@file:Suppress("LeakingThis", "unused")

package dev.zieger.utils.delegates

import dev.zieger.utils.coroutines.Continuation
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.withTimeout
import dev.zieger.utils.delegates.OnChangedParams2.Companion.DEFAULT_RECENT_VALUE_BUFFER_SIZE
import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.base.IDurationEx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * [ReadWriteProperty] with support for a listener that is called when the observed property changes.
 *
 * @property value property that will notify listener when it changes
 * @param storeRecentValues If set to `true` all values of the property are stored and provided within the
 * [IOnChangedScope2]. Should be set to `false` when the values of the property consume too much memory.
 * @property notifyForInitial When `true` a new listener will immediately notified for the existing value of the
 * property without the need of a change.
 * @property notifyOnChangedValueOnly When `false` listener are only notified when the value of the property changed.
 * When `true` every "set" to the property will notify the listener.
 * @property scope [CoroutineScope] that is used to notify listener that requested to be notified within a coroutine.
 * @property mutex If not `null` the [Mutex] will wrap the whole execution of [scope].
 */
interface IOnChanged2<P : Any?, T : Any?> : IOnChangedParams2<P, T>, ReadWriteProperty<P, T> {

    var value: T

    /**
     * When [recentValueSize] is greater than 0 this [List] contains the last [recentValueSize] changed values since
     * the last [clearRecentValues] invocation.
     */
    val recentValues: FiFo<T?>

    /**
     * Is invoked before every change of the property. When returning `true` the new value is not assigned
     * to the property.
     */
    fun vetoInternal(value: T): Boolean

    /**
     * Is invoked directly after `veto` if it returned `false`.
     * Maps the new input value to the new internal value.
     */
    fun mapInternal(value: T): T = value

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

typealias OnChanged<T> = OnChanged2<Any?, T>

/**
 * Implementation of [IOnChanged2] using [IOnChangedParams2] for initialization.
 */
open class OnChanged2<P : Any?, T : Any?>(
    params: IOnChangedParams2<P, T>
) : IOnChanged2<P, T>, IOnChangedParams2<P, T> by params {

    /**
     * @param initial The observed property will be initialized with this value.
     * @param storeRecentValues If set to `true` all values of the property will be stored and provided within the
     * [IOnChangedScope2]. Should be set to `false` when the values of the property consume too much memory.
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
        veto: (T) -> Boolean = { false },
        map: (T) -> T = { it },
        onChangedS: suspend IOnChangedScope2<P, T>.(T) -> Unit = {},
        onChanged: IOnChangedScope2<P, T>.(T) -> Unit = {}
    ) : this(OnChangedParams2(initial, storeRecentValues, recentValueSize, notifyForInitial, notifyOnChangedValueOnly,
        scope, mutex, veto, map, onChangedS, onChanged))

    protected var previousThisRef = AtomicReference<P?>(null)
    override val recentValues = FiFo<T?>(recentValueSize)

    override var value: T = initial
        set(newValue) {
            val vetoActive = vetoInternal(newValue)
            if (vetoActive) return
            val mappedInput = mapInternal(newValue)
            if (field != mappedInput || !notifyOnChangedValueOnly) {
                val old = field
                field = mappedInput
                onPropertyChanged(value, old)
            }
        }

    init {
        if (notifyForInitial) OnChangedScope2(
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
        OnChangedScope2(
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
    ) = OnChangedScope2(new, previousThisRef.get(), old, recentValues, { clearRecentValues() }, isInitialNotification)
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

    override fun mapInternal(value: T): T = map(value)

    override suspend fun IOnChangedScope2<P, T>.onChangedSInternal(value: T) = onChangedS(value)

    override fun IOnChangedScope2<P, T>.onChangedInternal(value: T) = onChanged(value)
}
