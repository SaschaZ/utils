@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package de.gapps.utils.coroutines.scope

import de.gapps.utils.misc.runEach
import kotlinx.coroutines.cancel

object GlobalScopeHolder {

    val MAIN = MainCoroutineScope()
    val DEFAULT = DefaultCoroutineScope()
    val IO = IoCoroutineScope()

    private val scopes = listOf(MAIN, DEFAULT, IO)
    fun reset() = scopes.runEach { reset() }
    fun cancel() = scopes.runEach { cancel() }
}