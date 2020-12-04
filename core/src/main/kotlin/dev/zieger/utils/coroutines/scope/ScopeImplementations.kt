@file:Suppress("unused")

package dev.zieger.utils.coroutines.scope

import dev.zieger.utils.coroutines.scope.ICoroutineScopeEx.Companion.DEFAULT_NAME
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

open class DefaultCoroutineScope(
    scopeName: String = DEFAULT_NAME,
    parent: CoroutineContext = Dispatchers.Default,
    useSupervisorJob: Boolean = false,
    onException: ((context: CoroutineContext, throwable: Throwable) -> Unit)? = null
) : CoroutineScopeEx(scopeName, parent, useSupervisorJob, onException)

open class IoCoroutineScope(
    scopeName: String = DEFAULT_NAME,
    parent: CoroutineContext = Dispatchers.IO,
    useSupervisorJob: Boolean = false,
    onException: ((context: CoroutineContext, throwable: Throwable) -> Unit)? = null
) : CoroutineScopeEx(scopeName, parent, useSupervisorJob, onException)

open class UnconfinedCoroutineScope(
    scopeName: String = DEFAULT_NAME,
    parent: CoroutineContext = Dispatchers.Unconfined,
    useSupervisorJob: Boolean = false,
    onException: ((context: CoroutineContext, throwable: Throwable) -> Unit)? = null
) : CoroutineScopeEx(scopeName, parent, useSupervisorJob, onException)