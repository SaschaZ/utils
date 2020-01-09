package de.gapps.utils.misc

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.duration.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface ICodeProxy {

    fun execute(code: () -> Unit)
}

class AntiSpamProxy(
    private val updateIntervalDuration: IDurationEx,
    private val scope: CoroutineScope
) : ICodeProxy {

    private var job: Job? = null
    private var lastExecuteTime: ITimeEx? = null

    override fun execute(code: () -> Unit) {
        job?.cancel()
        val current = TimeEx()
        val before = lastExecuteTime
        val aboveAllowedDuration = before?.let { current > it + updateIntervalDuration } != false
        if (aboveAllowedDuration) {
            lastExecuteTime = current
            code()
        } else {
            job = scope.launchEx(delayed = (before!! - current).toDuration()) { code() }
        }
    }
}

