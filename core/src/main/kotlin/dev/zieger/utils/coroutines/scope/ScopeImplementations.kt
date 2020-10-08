package dev.zieger.utils.coroutines.scope

import dev.zieger.utils.coroutines.scope.ICoroutineScopeEx.Companion.DEFAULT_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestCoroutineDispatcher


open class DefaultCoroutineScope(
    scopeName: String = DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.Default)

open class IoCoroutineScope(
    scopeName: String = DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.IO)

open class UnconfinedCoroutineScope(
    scopeName: String = DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.Unconfined)