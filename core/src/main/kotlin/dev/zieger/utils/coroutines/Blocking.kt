package dev.zieger.utils.coroutines

import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

val threadPool: ExecutorService = Executors.newFixedThreadPool(4)

/**
 * Suspends the current coroutine and Executes [block] in the [threadPool]. Will suspend as long as [block] is active
 * and returns the return value of [block]. Exceptions are forwarded to the caller.
 *
 * Useful when blocking code should be executed within a suspend method.
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