package dev.zieger.utils.coroutines.scope

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

open class CoroutineScopeEx(
    private val scopeName: String,
    private val parent: CoroutineContext,
    private val useSupervisorJob: Boolean = false,
    private val onException: ((context: CoroutineContext, throwable: Throwable) -> Unit)? = null
) : ICoroutineScopeEx {

    private val job: Job
        get() = coroutineContext[Job]!!

    override val coroutineContext: CoroutineContext
        get() = _coroutineContext
    private var _coroutineContext = newCoroutineContext

    private val newCoroutineContext: CoroutineContext
        get() = (parent + CoroutineName(scopeName) + if (useSupervisorJob) SupervisorJob() else Job()).run {
            onException?.let { this + CoroutineExceptionHandler { context, throwable -> it(context, throwable) } }
                ?: this
        }

    override suspend fun cancelAndJoin() = job.cancelAndJoin()

    override suspend fun join() = job.join()

    override fun reset() {
        cancel()
        _coroutineContext = newCoroutineContext
    }
}