package dev.zieger.utils.gui.console

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.exec
import dev.zieger.utils.gui.console.ConsoleProgressBar.Companion.PROGRESS_COLORS
import dev.zieger.utils.gui.console.LanternaConsole.Companion.refresh
import dev.zieger.utils.gui.console.ProcessInfo.Companion.DEFAULT_AUTO_UPDATE_INTERVAL
import dev.zieger.utils.gui.console.ProgressEntity.*
import dev.zieger.utils.gui.console.ProgressUnit.Bytes
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.lang.management.ManagementFactory

class ProcessInfo(
    private val scope: CoroutineScope,
    autoUpdateInterval: IDurationEx? = DEFAULT_AUTO_UPDATE_INTERVAL,
    private val onUpdate: () -> Unit = {}
) {

    companion object {

        internal val DEFAULT_AUTO_UPDATE_INTERVAL = 1.seconds

        data class TopData(
            val pid: Long,
            val cpuPercent: Double,
            val memPercent: Double,
            val mem: Long,
            val totalMem: Long = -1
        ) {
            constructor(raw: List<String>) :
                    this(
                        pid = raw[0].toLong(),
                        cpuPercent = raw[1].toDouble(),
                        memPercent = raw[2].toDouble(),
                        mem = raw[3].toLong()
                    )
        }
    }

    private val pid: Long get() = ManagementFactory.getRuntimeMXBean().name.split("@").first().toLong()

    private var autoUpdateJob: Job? = null

    init {
        autoUpdateInterval?.also {
            autoUpdateJob = scope.launchEx(interval = it) { info() }
        }
    }

    var cachedInfo: TopData? = null
        private set

    suspend fun info(): TopData? = "ps -ax -o pid,%cpu,%mem,rss,comm | grep $pid".exec()
        .stdOut.trim().split("\n").last()
        .replace(",", ".").split(" ")
        .filter { it.isNotBlank() }
        .nullWhen { it.size != 5 }
        ?.let { TopData(it) }
        ?.let { data ->
            data.copy(totalMem = "sysctl -n hw.memsize".exec().stdOut.toLong()).also {
                cachedInfo = it
                onUpdate()
            }
        }

    fun release() = autoUpdateJob?.cancel().asUnit()
}

fun main() = runBlocking {
    LanternaConsole().scope {
        outnl(SysInfo())
        delay(60.seconds)
    }
}

@Suppress("FunctionName")
fun CoroutineScope.SysInfo(
    autoUpdateInterval: IDurationEx? = DEFAULT_AUTO_UPDATE_INTERVAL,
    onUpdate: () -> Unit = { refresh() }
): List<TextWithColor> {
    val top = ProcessInfo(scope = this, autoUpdateInterval = autoUpdateInterval, onUpdate = onUpdate)
    val cpuProg = ProgressSource(this)
    val memProg = ProgressSource(this, unit = Bytes)
    return PROGRESS(cpuProg, Text {
        listOf(text {
            done = (top.cachedInfo?.cpuPercent?.let { it * 10 }?.toLong() ?: 0) /
                    Runtime.getRuntime().availableProcessors()
            total = 1000
            "CPU "
        })
    }, Bar(size = 17, foreground = PROGRESS_COLORS.reversed())) + PROGRESS(memProg, Text {
        listOf(text {
            done = (top.cachedInfo?.mem ?: 0) * 1000
            total = top.cachedInfo?.totalMem ?: 0
            " - MEM "
        })
    }, Bar(size = 17, foreground = PROGRESS_COLORS.reversed()), Space, Done())
}