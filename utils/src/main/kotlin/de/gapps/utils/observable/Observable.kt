package de.gapps.utils.observable

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.scope.DefaultCoroutineScope
import de.gapps.utils.delegates.OnChanged
import de.gapps.utils.misc.name
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel

/**
 * Container that provides observing of the internal variable of type [T].
 *
 * @param T Type of the internal value.
 * @param initial Initial value for the internal variable. Will not notify any observers.
 * @property scope [CoroutineScope] That is used to notify asynchronous observers. By default a scope with the
 * [Dispatchers.Default] Coroutine context is used.
 * @property onlyNotifyOnChanged Only notify observer when the internal value changes. Active by default.
 * @property storeRecentValues Stores all values and provide them in the onChanged callback. Active by default.
 * @property subscriberStateChanged Is invoked when an observer is added or removed.
 * @param onChanged Callback that is invoked when the internal values changes.
 */
open class Observable<out T>(
    initial: T,
    private val scope: CoroutineScope =
        DefaultCoroutineScope(Observable::class.name),
    private val onlyNotifyOnChanged: Boolean = true,
    private val storeRecentValues: Boolean = false,
    private val subscriberStateChanged: ((Boolean) -> Unit)? = null,
    onChanged: ChangeObserver<T> = {}
) : IObservable<T> {

    private val syncSubscribers = ArrayList<ChangeObserver<T>>()
    private val asyncSubscribers = ArrayList<SendChannel<T>>()

    private val recentValues = ArrayList<T>()

    override var value: @UnsafeVariance T = initial
        set(newValue) {
            if (newValue != field || !onlyNotifyOnChanged) {
                val recentValue = field
                if (storeRecentValues) recentValues.add(recentValue)

                field = newValue

                ArrayList(syncSubscribers).forEach {
                    recentValue.run {
                        ObservedChangedScope(newValue, recentValue, recentValues).it(newValue)
                    }
                }
                ArrayList(asyncSubscribers).forEach { c ->
                    scope.launchEx { c.send(newValue) }
                }
            }
        }

    init {
        @Suppress("LeakingThis")
        observe(onChanged)
    }

    override fun observe(listener: ChangeObserver<T>): () -> Unit {
        syncSubscribers.add(listener)
        if (syncSubscribers.size > 1) updateSubscriberState()
        return {
            syncSubscribers.remove(listener)
            updateSubscriberState()
        }
    }

    override fun observe(channel: SendChannel<T>) {
        asyncSubscribers.add(channel)
        updateSubscriberState()
    }

    private fun updateSubscriberState() {
        subscribersAvailable = syncSubscribers.size > 1 || asyncSubscribers.isNotEmpty()
    }

    private var subscribersAvailable by OnChanged(false) { new ->
        subscriberStateChanged?.invoke(new)
    }
}

