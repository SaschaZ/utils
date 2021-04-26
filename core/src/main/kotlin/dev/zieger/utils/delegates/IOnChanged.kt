@file:Suppress("unused")

package dev.zieger.utils.delegates

import dev.zieger.utils.misc.FiFo
import dev.zieger.utils.misc.IReleasable
import dev.zieger.utils.time.duration.IDurationEx
import kotlin.properties.ReadWriteProperty

typealias IOnChanged<T> = IOnChangedWithParent<Any?, T>

/**
 * [ReadWriteProperty] with support for a listener that is called when the observed property changes.
 */
interface IOnChangedWithParent<P : Any?, T : Any?> : IOnChangedParamsWithParent<P, T>,
    ReadWriteProperty<P, T>, IReleasable {

    /**
     * Property that will notify listener when it changes.
     */
    var value: T

    /**
     * Suspend call to change the property in a thread safe way. [safeSet] should be enabled for full safety.
     */
    suspend fun changeValue(block: (T) -> T)

    /**
     * When [previousValueSize] is greater than 0 this [List] contains the last [previousValueSize] changed values since
     * the last [clearPreviousValues] invocation.
     */
    val previousValues: FiFo<T?>

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
    suspend fun suspendUntilNextChange(
        timeout: IDurationEx? = null,
        onChanged: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit = {}
    ): T?

    /**
     * Suspends until the observed property changes to [wanted].
     */
    suspend fun suspendUntil(
        wanted: T,
        timeout: IDurationEx? = null,
        onChanged: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit = {}
    )

    /**
     * Suspend on change callback. Only is invoked when [scope] is set.
     * The [IOnChangedScopeWithParent] provides access to the previous values of this property (if [storeRecentValues] is `true`)
     * and the property holding object instance [P].
     */
    suspend fun IOnChangedScopeWithParent<P, T>.onChangedSInternal(value: T)

    /**
     * Unsuspended on change callback. Will be called immediately when a new value is set.
     * The [IOnChangedScopeWithParent] provides access to the previous values of this property (if [storeRecentValues] is `true`)
     * and the property holding object instance [P].
     */
    fun IOnChangedScopeWithParent<P, T>.onChangedInternal(value: T)

    /**
     * Clears the previous value storage.
     */
    fun clearPreviousValues()

    /**
     * Stop notifying for changes.
     * Call this than this class is not needed anymore.
     */
    override fun release()
}

