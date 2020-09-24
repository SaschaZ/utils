package dev.zieger.utils.observable

import dev.zieger.utils.delegates.IOnChangedParamsWithParent
import dev.zieger.utils.delegates.IOnChangedScopeWithParent
import dev.zieger.utils.misc.DataClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex

class ObservableParamsWithParent<P : Any?, T : Any?>(
    override val initial: T,
    override val scope: CoroutineScope? = null,
    storeRecentValues: Boolean = false,
    override val previousValueSize: Int = if (storeRecentValues) 100 else 0,
    override val notifyForInitial: Boolean = false,
    override val notifyOnChangedValueOnly: Boolean = true,
    override val mutex: Mutex = Mutex(),
    override val safeSet: Boolean = false,
    override val onSubscriberStateChanged: ((Boolean) -> Unit)? = {},
    override val veto: (T) -> Boolean = { false },
    override val map: (T) -> T = { it },
    override val onChangedS: suspend IOnChangedScopeWithParent<P, T>.(T) -> Unit = {},
    override val onChanged: IOnChangedScopeWithParent<P, T>.(T) -> Unit = {}
) : DataClass(), IObservableParamsWithParent<P, T>

interface IObservableParamsWithParent<P : Any?, T : Any?> : IOnChangedParamsWithParent<P, T> {
    val onSubscriberStateChanged: ((Boolean) -> Unit)?
}

typealias ObservableParams<T> = ObservableParamsWithParent<Any?, T>