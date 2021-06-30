package dev.zieger.utils.coroutines.scope

import kotlinx.coroutines.CoroutineScope

interface ICoroutineScopeEx : CoroutineScope {

    companion object {

        const val DEFAULT_NAME = "UNNAMED SCOPE"
    }

    suspend fun cancelAndJoin()
    suspend fun join()
    fun reset()
}