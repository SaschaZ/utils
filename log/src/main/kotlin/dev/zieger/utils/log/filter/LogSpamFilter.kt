package dev.zieger.utils.log.filter

import dev.zieger.utils.log.ILogContext
import dev.zieger.utils.log.ILogMessageContext
import dev.zieger.utils.time.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

//fun ILogContext.addTraceSpamFilter(
//    spamDuration: ITimeSpan = 1.seconds,
//    maximumSilence: ITimeSpan? = spamDuration * 10,
//    includeTags: List<Any> = emptyList(),
//    excludeTags: List<Any> = emptyList(),
//    buildKey: ILogMessageContext.(List<StackTraceElement>) -> String = { it.callOrigin() }
//) {
//    require(maximumSilence?.let { spamDuration < it } != false)
//
//    val single = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
//    val jobs = ConcurrentHashMap<String, Job>()
//    val lastMessage = ConcurrentHashMap<String, ITimeStamp>()
//    val firstDelayedMessage = ConcurrentHashMap<String, ITimeStamp>()
//    val delayedCancelable = ConcurrentHashMap<String, ICancellable>()
//
//    fun ILogMessageContext.storeMessageTime(origin: String, noSpam: Boolean) {
//        val now = TimeStamp()
//        if (noSpam) {
//            lastMessage[origin] = now
//            firstDelayedMessage.remove(origin)
//        }
//    }
//
//    fun ILogMessageContext.delayLogMessage(
//        origin: String,
//        deltaPrevCall: ITimeSpan,
//        next: () -> Unit
//    ) {
//        jobs[origin]?.cancel()
//        delayedCancelable[origin]?.cancel()
//        delayedCancelable[origin] = this
//        firstDelayedMessage[origin] = firstDelayedMessage[origin] ?: TimeStamp()
//        val tooLongSilence = firstDelayedMessage[origin]
//            ?.let { maximumSilence?.let { ms -> TimeStamp() - it >= ms } } == true
//
//        if (tooLongSilence) {
//            firstDelayedMessage.remove(origin)
//            next()
//        } else {
//            jobs[origin] = single.launch {
//                delay(spamDuration + 1.seconds)
//
//                delayed = true
//                val noSpam = lastMessage[origin]
//                    ?.let { TimeStamp() - it >= spamDuration } != false
//                storeMessageTime(origin, noSpam)
//                if (noSpam) next()
//                else cancel()
//            }
//        }
//    }
//
//    addPreFilter { next ->
//        val exclude = excludeTags.isNotEmpty() && (messageTag ?: tag) in excludeTags
//        val include = includeTags.isEmpty() || (messageTag ?: tag) in includeTags
//        if (exclude || !include) {
//            next()
//            return@addPreFilter
//        }
//
//        val origin = buildKey(Exception().stackTrace.toList())
//        single.launch {
//            lastMessage[origin].let { prev ->
//                val deltaPrevCall = createdAt - (prev ?: (createdAt - 1.hours))
//                val prevIsDelayed = jobs[origin]?.isActive == true
//                if (!prevIsDelayed && deltaPrevCall > spamDuration) {
//                    storeMessageTime(origin, true)
//                    next()
//                } else delayLogMessage(origin, deltaPrevCall) {
//                    next()
//                }
//            }
//        }
//    }
//}

fun ILogContext.addLogSpamFilter(
    spamDuration: ITimeSpan = 5.seconds,
    includeTags: List<Any> = emptyList(),
    excludeTags: List<Any> = emptyList(),
    scope: CoroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    ),
    buildKey: ILogMessageContext.() -> Any = { callOrigin() }
): Unit = LogSpamFiler(spamDuration, includeTags, excludeTags, scope, buildKey).run {
    addSpamFilter()
}

private class LogSpamFiler(
    private val spamDuration: ITimeSpan = 1.seconds,
    private val includeTags: List<Any> = emptyList(),
    private val excludeTags: List<Any> = emptyList(),
    private val scope: CoroutineScope = CoroutineScope(
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    ),
    private val buildKey: ILogMessageContext.() -> Any = { callOrigin() }
) {
    private val channel = Channel<
            Triple<ILogMessageContext, ILogMessageContext.() -> Unit, Any>
            >(Channel.UNLIMITED)

    private val lastSendCreatedAt = HashMap<Any, ITimeStamp>()
    private val delayedMessages = HashMap<Any, () -> Unit>()

    init {
        scope.launch {
            for ((ctx, next, key) in channel)
                ctx.processMessage(next, key)
        }
    }

    fun ILogContext.addSpamFilter() {
        addPreFilter { next ->
            val t = (messageTag ?: tag)
            val exclude = excludeTags.isNotEmpty() && t in excludeTags
            val include = includeTags.isEmpty() || t in includeTags
            if (!exclude && include)
                channel.trySend(
                    Triple(this, next, buildKey())
                )
            else next()
        }
    }

    private fun ILogMessageContext.processMessage(
        next: ILogMessageContext.() -> Unit,
        key: Any
    ) {
        val noSpam = lastSendCreatedAt[key]?.let {
            createdAt - it >= spamDuration
        } != false

        if (noSpam) {
            delayedMessages[key]?.invoke()
            lastSendCreatedAt[key] = createdAt
            next()
        } else delayMessage(next, key)
    }

    private fun ILogMessageContext.delayMessage(
        next: ILogMessageContext.() -> Unit,
        key: Any
    ) {
        delayedMessages[key]?.invoke()
        delayedMessages[key] = scope.launch {
            delay(lastSendCreatedAt[key]
                ?.let { spamDuration - (TimeStamp() - it) }
                ?.takeIf { it.notZero }
                ?: spamDuration)

            delayed = true
            next()
        }.let {
            {
                it.cancel()
                cancel()
            }
        }
    }
}

fun ILogMessageContext.callOrigin(
    vararg filterOut: String = arrayOf("dev.zieger.utils.log", "kotlin")
): String = (callOriginException ?: Exception().also { callOriginException = it })
    .stackTrace.filterNot { st ->
        filterOut.any {
            st.className.startsWith(it)
        }
    }.first().toString()