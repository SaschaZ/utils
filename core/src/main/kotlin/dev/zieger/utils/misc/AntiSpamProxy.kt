package dev.zieger.utils.misc

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.builder.withContextEx
import dev.zieger.utils.coroutines.scope.CoroutineScopeEx
import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.TimeEx
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.base.plus
import dev.zieger.utils.time.duration.IDurationEx
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