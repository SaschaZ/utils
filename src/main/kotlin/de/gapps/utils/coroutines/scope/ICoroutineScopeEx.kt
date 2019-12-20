package de.gapps.utils.coroutines.scope

import kotlinx.coroutines.CoroutineScope

interface ICoroutineScopeEx : CoroutineScope {

    companion object {

        internal const val DEFAULT_NAME = "UNNAMED SCOPE"
    }

    //    fun cancel()
    suspend fun cancelAndJoin()

    suspend fun join()
    fun reset()
}