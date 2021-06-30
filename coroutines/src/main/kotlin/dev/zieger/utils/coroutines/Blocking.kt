package dev.zieger.utils.coroutines

import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val threadPool: ExecutorService = Executors.newFixedThreadPool(4)

/**
 * Suspends the current coroutine and executes [block] inside [threadPool]. When [block] has finished execution the
 * coroutine is resumed.
 * This is useful for blocking calls inside a coroutine.
 */
suspend inline fun <T> executeNativeBlocking(crossinline block: () -> T): T = suspendCancellableCoroutine { cont ->
    threadPool.execute {
        try {
            cont.resume(block())
        } catch (e: Throwable) {
            if (!cont.isCompleted) cont.resumeWithException(e)
        }
    }
}