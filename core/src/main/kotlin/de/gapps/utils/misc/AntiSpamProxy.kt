package de.gapps.utils.misc

import de.gapps.utils.coroutines.builder.launchEx
import de.gapps.utils.coroutines.builder.withContextEx
import de.gapps.utils.coroutines.scope.CoroutineScopeEx
import de.gapps.utils.time.ITimeEx
import de.gapps.utils.time.TimeEx
import de.gapps.utils.time.base.minus
import de.gapps.utils.time.base.plus
import de.gapps.utils.time.duration.IDurationEx
import de.gapps.utils.time.duration.toDuration
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex

interface ICodeProxy : (suspend () -> Unit) -> Unit {

    fun execute(code: suspend () -> Unit)
    suspend fun coExecute(code: suspend () -> Unit)
}

class AntiSpamProxy(
    private val updateIntervalDuration: IDurationEx,
    private val scope: CoroutineScopeEx,
    private val mutex: Mutex = Mutex()
) : ICodeProxy {

    private var job: Job? = null
    private var lastExecuteTime: ITimeEx? = null

    override fun invoke(code: suspend () -> Unit) =
        scope.launchEx { coExecute { code() } }.asUnit()

    override fun execute(code: suspend () -> Unit) =
        scope.launchEx { coExecute { code() } }.asUnit()

    override suspend fun coExecute(code: suspend () -> Unit) = withContextEx(Unit, mutex = mutex) {
        job?.cancel()
        val current = TimeEx()
        val before = lastExecuteTime
        val aboveAllowedDuration = before?.let { current > it + updateIntervalDuration } != false
        if (aboveAllowedDuration) {
            lastExecuteTime = current
            code()
        } else {
            job = scope.launchEx(delayed = before!! - current, mutex = mutex) { code() }
        }
    }.asUnit()
}