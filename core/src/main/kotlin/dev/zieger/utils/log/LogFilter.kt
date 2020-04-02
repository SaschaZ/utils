package dev.zieger.utils.log

import dev.zieger.utils.time.duration.IDurationEx

sealed class LogFilter {
    open val id: String = ""
    open val minInterval: IDurationEx? = null
    open val resend: Boolean = true

    companion object {
        object NONE : LogFilter()

        data class GENERIC(
            override val id: String,
            override val minInterval: IDurationEx? = null,
            override val resend: Boolean = true,
            val onlyOnContentChange: Boolean = true
        ) : LogFilter()

        data class EXTERNAL(
            override val id: String,
            val callback: (LogLevel?, String) -> ExternalReturn
        ) : LogFilter() {
            companion object {
                sealed class ExternalReturn {
                    object OK : ExternalReturn()
                    object DENY : ExternalReturn()
                    class RECHECK(val duration: IDurationEx) : ExternalReturn()
                }
            }
        }
    }
}