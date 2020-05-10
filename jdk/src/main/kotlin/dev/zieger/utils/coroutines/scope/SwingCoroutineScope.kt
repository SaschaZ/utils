@file:Suppress("unused")

package dev.zieger.utils.coroutines.scope

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing


open class SwingCoroutineScope(
    scopeName: String = ICoroutineScopeEx.DEFAULT_NAME
) : CoroutineScopeEx(scopeName, Dispatchers.Swing)