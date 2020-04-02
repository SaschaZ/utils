package dev.zieger.utils.coroutines.scope

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

abstract class CoroutineScopeEx(
    private val scopeName: String,
    private val dispatcher: CoroutineDispatcher
) : ICoroutineScopeEx {


    private val job: Job
        get() = coroutineContext[Job]!!

    override val coroutineContext: CoroutineContext
        get() = _coroutineContext
    private var _coroutineContext = newCoroutineContext

    private val newCoroutineContext: CoroutineContext
        get() = Job() + dispatcher + CoroutineName(scopeName)

    override suspend fun cancelAndJoin() = job.cancelAndJoin()

    override suspend fun join() = job.join()

    override fun reset() {
        cancel()
        _coroutineContext = newCoroutineContext
    }
}