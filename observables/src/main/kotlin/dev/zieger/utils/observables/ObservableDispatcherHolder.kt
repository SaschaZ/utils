package dev.zieger.utils.observables

import kotlinx.coroutines.newSingleThreadContext

object ObservableDispatcherHolder {
    val context = newSingleThreadContext("ObservableDispatcher")
}