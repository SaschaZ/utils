package dev.zieger.utils.coroutines.trigger

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*

abstract class Trigger(
    private val scope: CoroutineScope,
    vararg depends: Observable,
    private val printDebug: Boolean = false,
    private val debugName: String? = null
) : Observable {

    init {
        runBlocking {
            depends.forEach {
                it.values { dependentChanged() }
            }
        }
    }

    protected abstract suspend fun dependentChanged()

    private val name
        get() = "${debugName ?: "value"}(#${System.identityHashCode(this)})"

    private val observer = LinkedList<suspend () -> Unit>()
    private val observerMutex = Mutex()

    private var triggerJob: Job? = null

    protected open fun trigger() {
        triggerJob?.cancel()
        triggerJob = scope.launch { triggerInternal() }
    }

    protected open suspend fun triggerInternal() {
        val obs = ArrayList(observer)
        if (printDebug)
            println("$name content change (will notify ${obs.size} observer)")
        obs.forEach { it() }
    }

    override suspend fun values(block: suspend () -> Unit): suspend () -> Unit = observerMutex.withLock {
        observer += block
        return {
            observerMutex.withLock { observer -= block }
        }
    }
}