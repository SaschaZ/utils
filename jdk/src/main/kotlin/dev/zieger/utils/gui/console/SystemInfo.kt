package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.*
import dev.zieger.utils.coroutines.CommandOutput
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.exec
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.gui.console.ConsoleProgressBar
import dev.zieger.utils.gui.console.ConsoleProgressBar.Companion.PROGRESS_COLORS
import dev.zieger.utils.gui.console.LanternaConsole
import dev.zieger.utils.gui.console.LanternaConsole.Companion.lastInstance
import dev.zieger.utils.gui.console.ProgressEntity.*
import dev.zieger.utils.gui.console.TextWithColor
import dev.zieger.utils.misc.nullWhen
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.milliseconds
import dev.zieger.utils.time.duration.seconds
import dev.zieger.utils.time.duration.toDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.lang.management.ManagementFactory
import java.text.DecimalFormat

private val String.parseSize: Long
    get() = when (last()) {
        'g' -> (removeSuffix("g").toDouble() * 1024 * 1024 * 1024).toLong()
        'm' -> (removeSuffix("m").toDouble() * 1024 * 1024).toLong()
        'k' -> (removeSuffix("k").toDouble() * 1024).toLong()
        else -> toLong() * 1024
    }

class TopParser(
    private val scope: CoroutineScope,
    private val autoUpdateInterval: IDurationEx = 1.seconds,
    autoUpdateActive: Boolean = false
) {

    data class TopData(
        val pid: Long,
        val user: String,
        val pr: Int,
        val ni: Int,
        val virt: Long,
        val res: Long,
        val shr: Long,
        val s: String,
        val cpu: Double,
        val mem: Double,
        val time: IDurationEx,
        val command: String
    ) {
        constructor(raw: Array<String>) :
                this(
                    pid = raw[0].toLong(),
                    user = raw[1],
                    pr = raw[2].toInt(),
                    ni = raw[3].toInt(),
                    virt = raw[4].parseSize,
                    res = raw[5].parseSize,
                    shr = raw[6].parseSize,
                    s = raw[7],
                    cpu = raw[8].toDouble() / Runtime.getRuntime().availableProcessors(),
                    mem = raw[9].toDouble(),
                    time = 0.toDuration(),
                    command = raw[11]
                )

        var availMem: Long = -1L
        val memPercent: Double get() = res / (res + availMem).toDouble()
    }

    private val pid: Long get() = ManagementFactory.getRuntimeMXBean().name.split("@").first().toLong()

    private var autoUpdateJob: Job? = null
    var autoUpdateActive by OnChanged(autoUpdateActive, notifyForInitial = true) {
        autoUpdateJob?.cancel()
        if (it) autoUpdateJob = scope.launchEx(interval = autoUpdateInterval) { info() }
    }

    var cachedInfo: TopData? = null
        private set

    suspend fun info(): TopData? = "top -b -n 1 -p $pid | grep java".exec().wait()
        .stdOut.trim().lines().last()
        .replace(",", ".").split(" ")
        .filter { it.isNotBlank() }
        .nullWhen { it.size < 12 }
        ?.let { TopData(it.toTypedArray()) }
        ?.also { data ->
            val regex = Regex("([0-9,]+) (verfü|avail)")
            data.availMem = ("top -b -n 1 -p $pid".exec().output().stdOutput.trim()
                .let { regex.findAll(it) }.firstOrNull()?.groupValues?.getOrNull(1)
                ?.replace(",", ".")?.toDouble()
                ?.let { it * 1024 * 1024 } ?: -1.0).toLong()
            cachedInfo = data
        }
}

fun main() = runBlocking {
    val top = TopParser(this)
    top.autoUpdateActive = true
    repeat(15) { println(top.info()); delay(500.milliseconds) }
}

@Suppress("FunctionName")
fun CoroutineScope.SysInfo(): Array<TextWithColor> = lastInstance?.scope {
    val top = TopParser(scope = DefaultCoroutineScope(), autoUpdateActive = true)
    val cpuProg = ProgressSource(this@SysInfo, total = top.cachedInfo?.mem?.toLong() ?: 0)
    val memProg = ProgressSource(this@SysInfo)
    PROGRESS(cpuProg, Text {
        done = top.cachedInfo?.cpu?.let { it.toLong() / 100 } ?: 0
        listOf(+"CPU ")
    }, Bar(size = 17)) + PROGRESS(memProg, Text(" - MEM "), Bar(size = 17))
    arrayOf(WHITE {
        cpuProg.progressPercent = top.cachedInfo?.cpu?.let { it / 100.0 } ?: 0.0
        "CPU  "
    }, cpuProg.textWithColor, WHITE(" - MEM "), memProg.textWithColor,
        WHITE {
            memProg.progressPercent = top.cachedInfo?.memPercent ?: 0.0
            " ${DecimalFormat("0.0").format(top.cachedInfo?.res?.let { it / 1024.0 / 1024 } ?: 0.0)}MB\n"
        })
} ?: emptyArray()