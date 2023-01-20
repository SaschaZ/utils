package dev.zieger.utils.log.filter

import dev.zieger.utils.log.ICancellable
import dev.zieger.utils.log.ILogContext
import dev.zieger.utils.log.ILogMessageContext
import dev.zieger.utils.time.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.roundToInt

fun ILogContext.addTraceSpamFilter(
    spamDuration: ITimeSpan = 1.seconds,
    maximumSilence: ITimeSpan = spamDuration * 10,
    buildDelayedMessage: ILogMessageContext.(key: String) -> String = {
        "delayed by ${(TimeStamp() - createdAt).seconds.roundToInt()}s:\n\t${buildedMessage}"
    },
    buildKey: ILogMessageContext.(List<StackTraceElement>) -> String = { it.callOrigin() }
) {
    require(spamDuration < maximumSilence)

    val single = CoroutineScope(Executors.newSingleThreadExecutor().asCoroutineDispatcher())
    val jobs = ConcurrentHashMap<String, Job>()
    val lastMessage = ConcurrentHashMap<String, ITimeStamp>()
    val firstDelayedMessage = ConcurrentHashMap<String, ITimeStamp>()
    val delayedCancelable = ConcurrentHashMap<String, ICancellable>()

    fun ILogMessageContext.storeMessageTime(origin: String, noSpam: Boolean) {
        val now = TimeStamp()
        if (noSpam) {
            lastMessage[origin] = now
            firstDelayedMessage.remove(origin)
        }
    }

    fun ILogMessageContext.delayLogMessage(
        origin: String,
        deltaPrevCall: ITimeSpan,
        next: () -> Unit
    ) {
        jobs[origin]?.cancel()
        delayedCancelable[origin]?.cancel()
        delayedCancelable[origin] = this
        firstDelayedMessage[origin] = firstDelayedMessage[origin] ?: TimeStamp()
        val tooLongSilence = firstDelayedMessage[origin]
            ?.let { TimeStamp() - it >= maximumSilence } == true

        if (tooLongSilence) {
            firstDelayedMessage.remove(origin)
            next()
        } else {
            jobs[origin] = single.launch {
                delay(spamDuration + 1.seconds)

                buildedMessage = buildDelayedMessage(origin)
                val noSpam = lastMessage[origin]
                    ?.let { TimeStamp() - it >= spamDuration } != false
                storeMessageTime(origin, noSpam)
                if (noSpam) next()
                else cancel()
            }
        }
    }

    addPostFilter { next ->
        val origin = buildKey(Exception().stackTrace.toList())
        single.launch {
            lastMessage[origin].let { prev ->
                val deltaPrevCall = createdAt - (prev ?: (createdAt - 1.hours))
                val prevIsDelayed = jobs[origin]?.isActive == true
                if (!prevIsDelayed && deltaPrevCall > spamDuration) {
                    storeMessageTime(origin, true)
                    next()
                } else delayLogMessage(origin, deltaPrevCall) {
                    next()
                }
            }
        }
    }
}

fun List<StackTraceElement>.callOrigin(): String =
    filterNot { st ->
        st.className.run {
            startsWith("dev.zieger.utils.log", true)
                    || startsWith("kotlin", true)
                    || startsWith("io.kotest", true)
        }
    }.first().toString()

fun String.clickablePart(): String = substring(indexOf('('), length)