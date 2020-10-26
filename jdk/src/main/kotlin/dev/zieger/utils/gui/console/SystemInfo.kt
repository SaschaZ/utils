package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TextColor
import dev.zieger.utils.coroutines.CommandOutput
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.gui.console.ConsoleProgressBar
import dev.zieger.utils.gui.console.LanternaConsole
import dev.zieger.utils.gui.console.LanternaConsole.Companion.lastInstance
import dev.zieger.utils.gui.console.TextWithColor
import dev.zieger.utils.gui.console.invoke
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
                    raw[0].toLong(), raw[1], raw[2].toInt(), raw[3].toInt(), raw[4].parseSize, raw[5].parseSize,
                    raw[6].parseSize, raw[7], raw[8].toDouble() / Runtime.getRuntime().availableProcessors(),
                    raw[9].toDouble(), 0.toDuration(), raw[11]
                )

        var availMem: Long = -1L
        val memPercent: Double get() = res / (res + availMem).toDouble()
    }

    private val pid: Long get() = ManagementFactory.getRuntimeMXBean().name.split("@").first().toLong()

    private var autoUpdateJob: Job? = null
    var autoUpdateActive by OnChanged(autoUpdateActive, notifyForInitial = true) {
        autoUpdateJob?.cancel()
        if (it)
            autoUpdateJob = scope.launchEx(interval = autoUpdateInterval) { info() }
    }

    var cachedInfo: TopData? = null
        private set

    fun info(): TopData? = "top -b -n 1 -p $pid | grep java".exec()
        .stdOutput.trim().lines().last()
        .replace(",", ".").split(" ")
        .filter { it.isNotBlank() }
        .nullWhen { it.size < 12 }
        ?.let { TopData(it.toTypedArray()) }
        ?.also { data ->
            val regex = Regex("([0-9,]+) (verfü|avail)")
            data.availMem = ("top -b -n 1 -p $pid".exec().stdOutput.trim()
                .let { regex.findAll(it) }.firstOrNull()?.groupValues?.getOrNull(1)
                ?.replace(",", ".")?.toDouble()
                ?.let { it * 1024 * 1024 } ?: -1.0).toLong()
            cachedInfo = data
        }
}

fun String.exec(): CommandOutput = Runtime.getRuntime().exec(arrayOf("/bin/sh", "-c", this)).run {
    waitFor()
    CommandOutput(exitValue(), inputStream.reader().readText(), errorStream.reader().readText())
}

fun main() = runBlocking {
    val top = TopParser(this)
    top.autoUpdateActive = true
    repeat(15) { println(top.info()); delay(500.milliseconds) }
}

@Suppress("FunctionName")
fun SysInfo(): Array<TextWithColor> = lastInstance?.scope {
    val top = TopParser(scope = DefaultCoroutineScope(), autoUpdateActive = true)
    val cpuProg =
        ConsoleProgressBar(size = 17, foreground = ConsoleProgressBar.PROGRESS_COLORS.reversed()) { refresh() }
    val memProg =
        ConsoleProgressBar(size = 17, foreground = ConsoleProgressBar.PROGRESS_COLORS.reversed()) { refresh() }
    arrayOf(TextColor.ANSI.WHITE {
        cpuProg.progressPercent = top.cachedInfo?.cpu?.let { it / 100.0 } ?: 0.0
        "CPU  "
    }, cpuProg.textWithColor, TextColor.ANSI.WHITE(" - MEM "), memProg.textWithColor,
        TextColor.ANSI.WHITE {
            memProg.progressPercent = top.cachedInfo?.memPercent ?: 0.0
            " ${DecimalFormat("0.0").format(top.cachedInfo?.res?.let { it / 1024.0 / 1024 } ?: 0.0)}MB\n"
        })
} ?: emptyArray<TextWithColor>()