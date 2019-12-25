package de.gapps.utils.coroutines.scope

import de.gapps.utils.coroutines.scope.ICoroutineScopeEx.Companion.DEFAULT_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing


open class DefaultCoroutineScope(
    scopeName: String = DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.Default)

open class MainCoroutineScope(
    scopeName: String = DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.Main)

open class IoCoroutineScope(
    scopeName: String = DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.IO)

open class UnconfinedCoroutineScope(
    scopeName: String = DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.Unconfined)

open class SwingCoroutineScope(
    scopeName: String = DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.Swing)