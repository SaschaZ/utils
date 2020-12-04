package dev.zieger.utils.coroutines.scope

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

open class MainCoroutineScope(
    scopeName: String = ICoroutineScopeEx.DEFAULT_NAME,
    parent: CoroutineContext = Dispatchers.Main,
    useSupervisorJob: Boolean = false,
    onException: (context: CoroutineContext, throwable: Throwable) -> Unit = { _, _ -> }
) : CoroutineScopeEx(scopeName, parent, useSupervisorJob, onException)