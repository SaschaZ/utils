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

class TopInfo(
    private val scope: CoroutineScope,
    autoUpdateInterval: IDurationEx? = DEFAULT_AUTO_UPDATE_INTERVAL,
    private val onUpdate: TopInfo.() -> Unit = {}
) {

    companion object {

        internal val DEFAULT_AUTO_UPDATE_INTERVAL = 1.seconds
    }

    var cpuPercent: Double = 0.0
        private set
    var memPercent: Double = 0.0
        private set
    var totalMem: Double = 0.0
        private set
    var overallUsedMem: Double = 0.0
        private set
    var usedMem: Double = 0.0
        private set
    var availMem: Double = 0.0
        private set

    private var cachedOutput: String? = null

    private var autoUpdateJob: Job? = null

    private val pid: Long get() = ManagementFactory.getRuntimeMXBean().name.split("@").first().toLong()

//    init {
//        autoUpdateInterval?.also {
//            autoUpdateJob = scope.launchEx(interval = it) { update() }
//        }
//    }

//    suspend fun update() {
//        cachedOutput = null
//        cpuPercent = determineCpuPercent()
//        memPercent = determineMemPercent()
//        onUpdate()
//    }
//
//    private suspend fun determineCpuPercent(): Double = when (OsInfo.type) {
//        MACOS -> "CPU usage: ([0-9.]+)% user, ([0-9.]+)% sys, ([0-9.]+)% idle".toRegex()
//            .find(cachedOutput ?: "top -l 1 -n 1".runCommand().stdOutput.also { cachedOutput = it })?.groupValues?.run {
//                whenNotNull(getOrNull(1)?.toDoubleOrNull(), getOrNull(2)?.toDoubleOrNull()) { user, sys ->
//                    (user + sys) / 100
//                }
//            }
//        LINUX -> "%Cpu\\(s\\): {2}([0-9,.]+) us, {2}([0-9,.]+) sy, {2}([0-9,.]+) ni, ([0-9,.]+) id".toRegex()
//            .find("top -n 1 -1".runCommand().stdOutput)?.groupValues?.run {
//                whenNotNull(getOrNull(1)?.toDoubleOrNull(), getOrNull(2)?.toDoubleOrNull()) { user, sys ->
//                    (user + sys) / 100
//                }
//            }
//        else -> null
//    } ?: -1.0
//
//    private suspend fun updateAll() {
//        when (OsInfo.type) {
//            MACOS -> ("""[\w]+: ([\d]+)([KMG]?) [\w]+ \(([\d]+)([KMG]?) [\w]+\), ([\d]+)([KMG]?) [\w]+.[\W\w]+[\w]+ +([\d.]+) +([\d]+)([KMG]?)([-+]?)""").toRegex()
//                .find(
//                    cachedOutput ?: "top -l 2 -stats command,cpu,mem -pid $pid".runCommand().stdOutput.also {
//                        cachedOutput = it
//                    })?.groupValues?.run {
//                    whenNotNull(
//                        getOrNull(5)?.toDoubleOrNull(), getOrNull(6),
//                        getOrNull(7)?.toDoubleOrNull(), getOrNull(8)?.toIntOrNull(), getOrNull(9)
//                    ) { unused, unusedUnit, cpu, mem, memUnit ->
//                        totalMem = "sysctl hw.memsize".runCommand().stdOutput.split(" ")[1].toDouble()
//                        availMem = unused * unusedUnit.unitFactor
//                        overallUsedMem = totalMem - availMem
//                        usedMem = mem * memUnit.unitFactor.toDouble()
//                        memPercent = overallUsedMem / totalMem
//                        cpuPercent = cpu
//                    }
//                }
//            LINUX -> ("Mem: *([0-9]*) *([0-9]*) *([0-9]*) *([0-9]*) *([0-9]*) *([0-9]*)").toRegex()
//                .find("free -m | grep Mem:".runCommand().stdOutput)?.groupValues?.run {
//                    whenNotNull(
//                        getOrNull(1)?.toDoubleOrNull(),
//                        getOrNull(2)?.toDoubleOrNull(),
//                        getOrNull(3)?.toDoubleOrNull()
//                    ) { total, used, avail ->
//                        totalMem = total * 1024 * 1024
//                        usedMem = used * 1024 * 1024
//                        availMem = avail * 1024 * 1024
//                        memPercent = usedMem / totalMem
//                    }
//                }
//            else -> Unit
//        }
//    }

    private val String.unitFactor
        get() = when (this) {
            "G" -> 1024 * 1024 * 1024
            "M" -> 1024 * 1024
            "K" -> 1024
            else -> 1
        }

    fun release() = autoUpdateJob?.cancel().asUnit()
}

class ProcessInfo(
    private val scope: CoroutineScope,
    autoUpdateInterval: IDurationEx? = DEFAULT_AUTO_UPDATE_INTERVAL,
    private val onUpdate: () -> Unit = {}
) {

    companion object {

        internal val DEFAULT_AUTO_UPDATE_INTERVAL = 5.seconds

        data class ProcessData(
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

    var cachedInfo: ProcessData? = null
        private set

    suspend fun info(): ProcessData? = "ps -ax -o pid,%cpu,%mem,rss,comm | grep $pid".exec()
        .stdOut.trim().split("\n").last()
        .replace(",", ".").split(" ")
        .filter { it.isNotBlank() }
        .nullWhen { it.size != 5 }
        ?.let { ProcessData(it) }
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
    onUpdate: TopInfo.() -> Unit = { refresh() }
): List<TextWithColor> {
    val top = TopInfo(scope = this, autoUpdateInterval = autoUpdateInterval, onUpdate = onUpdate)
    val cpuProg = ProgressSource(this)
    val memProg = ProgressSource(this, unit = Bytes)
    return PROGRESS(cpuProg, Text {
        listOf(text {
            done = ((top.cpuPercent * 1000).toLong())
            total = 1000
            "CPU "
        })
    }, Bar(size = 17, foreground = PROGRESS_COLORS.reversed())) + PROGRESS(memProg, Text {
        listOf(text {
            done = Runtime.getRuntime().run { totalMemory() - freeMemory() }
            total = Runtime.getRuntime().totalMemory()
            " - MEM "
        })
    }, Bar(size = 17, foreground = PROGRESS_COLORS.reversed()), Space, DoneOfTotal())
}