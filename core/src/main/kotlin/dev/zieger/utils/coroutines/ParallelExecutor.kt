@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.coroutines

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.mapPrev
import dev.zieger.utils.misc.runEach
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.joinAll
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.coroutineContext

/**
 * Iterates over a [List] of [I] and calls the [block] with each element to map them to the [List] of output types [O].
 * Every call to block is shared over a fixed set of [Channel]s to reduce execution time by using all available physical
 * CPU cores.
 *
 * @param I Input type of the executions.
 * @param O Output type of the executions.
 *
 * @param numParallel Defines how much simultaneous working channels should be used. Defaulting to the amount of
 * processors cores the system provides.
 * @param block Lambda to map the input values of type [I] to the output values of type [O].
 *
 * @return Sorted [List] of [block] results of type [O].
 */
suspend inline fun <I, O> Collection<I>.mapParallel(
    numParallel: Int = Runtime.getRuntime().availableProcessors(),
    crossinline block: suspend (I) -> O
): Collection<O> {
    return parallelScope(numParallel) {
        map { item -> execute { block(item) } }
        suspendAndReceive()
    }
}

suspend inline fun <I, O> Collection<I>.runParallel(
    numParallel: Int = Runtime.getRuntime().availableProcessors(),
    crossinline block: suspend I.() -> O
): Collection<O> = mapParallel(numParallel) { it.run { block() } }

suspend inline fun <I> Collection<I>.forEachParallel(
    numParallel: Int = Runtime.getRuntime().availableProcessors(),
    crossinline block: suspend (I) -> Unit
): Unit = mapParallel(numParallel) { block(it); Unit }.asUnit()

/**
 * Helper method to create a [ParallelExecutor] scope.
 *
 * @param R Return type of the executions.
 *
 * @param numParallel Defines how much simultaneous working channels should be used. Defaulting to the amount of
 * processors cores the system provides.
 * @param block Lambda with [ParallelExecutor] as receiver.
 */
suspend inline fun <R> parallelScope(
    numParallel: Int = Runtime.getRuntime().availableProcessors(),
    block: ParallelExecutor<R>.() -> List<R>
): List<R> = ParallelExecutor<R>(numParallel, CoroutineScope(coroutineContext)).block()

/**
 * Executor class to share multiple executions over a fixed amount of coroutines.
 *
 * @param R Return type of the executions.
 *
 * @param numParallel Defines how much simultaneous working channels should be used. Defaulting to the amount of
 * processors cores the system provides.
 * @property scope [CoroutineScope] the is used to create the need coroutines. Defaulting to [DefaultCoroutineScope].
 */
open class ParallelExecutor<R>(
    private val numParallel: Int = Runtime.getRuntime().availableProcessors(),
    private val scope: CoroutineScope = DefaultCoroutineScope()
) {
    private val resultChannel = Channel<Pair<Long, R>>(Channel.UNLIMITED)

    private val channels: List<SendChannel<Pair<suspend () -> R, suspend (R) -> Unit>>> =
        ArrayList<SendChannel<Pair<suspend () -> R, suspend (R) -> Unit>>>().apply {
            val finishCount = AtomicInteger(0)
            repeat(numParallel) { i ->
                add(
                    Channel<Pair<suspend () -> R, suspend (R) -> Unit>>().also { c ->
                        scope.launchEx {
                            var receiveCnt = 0L
                            for (b in c)
                                b.second(b.run {
                                    first().also { result ->
                                        resultChannel.send(i * receiveCnt++ to result)
                                    }
                                })

                            if (finishCount.incrementAndGet() == numParallel) resultChannel.close()
                        }
                    })
            }
        }

    private val idx = AtomicInteger(0)
    private val jobs = ArrayList<Job>()

    /**
     * Will execute the provided lambda.
     * All executions are distributed equally in the internal coroutine pool.
     *
     * @param block Lambda to execute.
     *
     * @return Lambda that suspends on invocation until the next result is available and returns that result.
     */
    fun execute(block: suspend () -> R): suspend () -> R {
        val cont = Channel<R>(Channel.CONFLATED)
        jobs.add(scope.launchEx {
            val channel = channels[idx.getAndIncrement() % numParallel]
            channel.send(block to { result -> cont.send(result) })
        })
        return { cont.receive() }
    }

    /**
     * Closes the internal channels.
     *
     * @param suspend `true` to suspend before closing channels. Defaulting to `true`.
     */
    suspend fun close(suspend: Boolean = true) {
        if (suspend) suspend()
        channels.runEach { close() }
    }

    /**
     * Suspends until all executed blocks have finished execution.
     * `close` should be called before.
     */
    suspend fun suspend() {
        while (jobs.any { it.isActive }) jobs.joinAll()
    }

    /**
     * Suspends until all executed blocks have finished execution and returns the results of the executions.
     * `close` should be called before.
     *
     * @return Sorted List of [R] instances.
     */
    suspend fun suspendAndReceive(): List<R> {
        close()
        val r = ArrayList<Pair<Long, R>>()
        for (result in resultChannel) r += result
        return r.sortedBy { it.first }.map { it.second }
    }

    /**
     * Resets this [ParallelExecutor] instance by clearing all jobs, results and setting the internal index to 0.
     */
    fun reset() {
        jobs.clear()
        idx.set(0)
    }
}

/**
 * Same as [mapParallel] with also providing the previous value in the [List] into the [block].
 *
 * @param I Input type of the executions.
 * @param O Output type of the executions.
 *
 * @param numParallel Defines how much simultaneous working channels should be used. Defaulting to the amount of
 * processors cores the system provides.
 * @param block Is called for every element pair (current and previous value) in the [List] to map the input to the
 * output.
 *
 * @return [List] of [block] results.
 */
suspend inline fun <I : Any, O : Any> List<I>.mapPrevParallel(
    numParallel: Int = Runtime.getRuntime().availableProcessors(),
    crossinline block: (cur: I, prev: I) -> O?
): List<O?> {
    val sizePerJob = size / numParallel
    val groups = (0 until numParallel).mapNotNull {
        if (it < numParallel - 1)
            subList(it * sizePerJob, if (it < numParallel - 2) (it + 1) * sizePerJob else lastIndex)
        else null
    }

    return parallelScope<List<O?>>(numParallel) {
        groups.forEach<List<I?>> { group ->
            execute {
                group.mapPrev(block = block)
            }
        }
        suspendAndReceive()
    }.combineGroups(groups, block)
}

inline fun <I, O> List<List<O>>.combineGroups(
    groups: List<List<I>>,
    block: (cur: I, prev: I) -> O?
): List<O?> {
    var idx = 0
    return flatMap {
        it + if (idx < lastIndex) listOf(block(groups[idx].last(), groups[++idx].first())) else emptyList()
    }
}