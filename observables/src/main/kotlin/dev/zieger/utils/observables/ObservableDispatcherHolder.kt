@file:Suppress("EXPERIMENTAL_API_USAGE")

package dev.zieger.utils.observables

import kotlinx.coroutines.newSingleThreadContext

object ObservableDispatcherHolder {
    var context = newSingleThreadContext("ObservableDispatcher")
}