package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChangedParamsWithParent
import dev.zieger.utils.delegates.IOnChangedScopeWithParent
import dev.zieger.utils.misc.DataClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

class ObservableParamsWithParent<P : Any?, T : Any?>(
    override val initial: T,
    override val buildScope: () -> CoroutineScope,
    storeRecentValues: Boolean = false,
    override val previousValueSize: Int = if (storeRecentValues) 100 else 0,
    override val notifyForInitial: Boolean = false,
    override val notifyOnChangedValueOnly: Boolean = true,
    override val mutex: Mutex = Mutex(),
    override val onSubscriberStateChanged: ((Int) -> Unit)? = {},
    override val veto: (T) -> Boolean = { false },
    override val map: (T) -> T = { it },
    override val onChangedS: (suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit)? = null
) : DataClass(), IObservableParamsWithParent<P, T> {

    constructor(
        initial: T,
        scope: CoroutineScope,
        storeRecentValues: Boolean = false,
        previousValueSize: Int = if (storeRecentValues) 100 else 0,
        notifyForInitial: Boolean = false,
        notifyOnChangedValueOnly: Boolean = true,
        mutex: Mutex = Mutex(),
        onSubscriberStateChanged: ((Int) -> Unit)? = {},
        veto: (T) -> Boolean = { false },
        map: (T) -> T = { it },
        onChangedS: (suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit)? = null
    ) : this(
        initial, { scope }, storeRecentValues, previousValueSize, notifyForInitial, notifyOnChangedValueOnly,
        mutex, onSubscriberStateChanged, veto, map, onChangedS
    )

    override val scope: CoroutineScope
        get() = buildScope()
    override val onChanged: (IOnChangedScopeWithParent<P, T>.(T) -> Unit) = {}
}

interface IObservableParamsWithParent<P : Any?, T : Any?> : IOnChangedParamsWithParent<P, T> {
    override val scope: CoroutineScope
    val buildScope: () -> CoroutineScope
        get() = { scope }
    val onSubscriberStateChanged: ((Int) -> Unit)?
}

typealias ObservableParams<T> = ObservableParamsWithParent<Any?, T>