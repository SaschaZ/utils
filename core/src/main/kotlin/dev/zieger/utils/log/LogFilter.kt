package dev.zieger.utils.log

import dev.zieger.utils.time.duration.IDurationEx
import kotlin.random.Random

sealed class LogFilter {
    open val id: String = ""
    open val minInterval: IDurationEx? = null
    open val resend: Boolean = true
    open val disableLog: Boolean = false

    companion object {
        private val idCache: MutableSet<String> = mutableSetOf()
        val newId: String
            get() {
                var id = "${Random.nextLong()}"
                do {
                    val alreadyTaken = idCache.contains(id)
                    if (alreadyTaken)
                        id = "${Random.nextLong()}"
                } while (alreadyTaken)
                idCache.add(id)
                return id
            }

        object NONE : LogFilter()

        data class GENERIC(
            override val id: String = newId,
            override val minInterval: IDurationEx? = null,
            override val resend: Boolean = true,
            val onlyOnContentChange: Boolean = true,
            override val disableLog: Boolean = false
        ) : LogFilter()

        data class EXTERNAL(
            override val id: String = newId,
            val callback: (LogLevel?, String) -> ExternalReturn
        ) : LogFilter() {
            companion object {
            }
        }
    }
}
sealed class ExternalReturn {
    object OK : ExternalReturn()
    object DENY : ExternalReturn()
    class RECHECK(val duration: IDurationEx) : ExternalReturn()
}