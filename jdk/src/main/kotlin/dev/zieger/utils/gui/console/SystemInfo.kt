package dev.zieger.utils.gui.console

import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.screen.Screen
import dev.zieger.utils.OsInfo
import dev.zieger.utils.OsInfo.OsType.LINUX
import dev.zieger.utils.OsInfo.OsType.MACOS
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.runCommand
import dev.zieger.utils.gui.console.GlobalConsoleScope.refresh
import dev.zieger.utils.gui.console.progress.ConsoleProgressBar.Companion.PROGRESS_COLORS
import dev.zieger.utils.gui.console.progress.PROGRESS
import dev.zieger.utils.gui.console.progress.ProgressEntity.*
import dev.zieger.utils.gui.console.progress.ProgressUnit
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.whenNotNull
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import java.lang.management.ManagementFactory
import java.util.*

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

    private var autoUpdateJob: Job? = null

    private val pid: Long = ManagementFactory.getRuntimeMXBean().name.split("@").first().toLong()

    init {
        autoUpdateInterval?.also {
            autoUpdateJob = scope.launchEx(interval = it) { update() }
        }
    }

    suspend fun update() {
        updateAll()
        onUpdate()
    }

    private suspend fun updateAll() {
        when (OsInfo.type) {
            MACOS -> ("""[\w]+: ([\d]+)([KMG]?) [\w]+ \(([\d]+)([KMG]?) [\w]+\), ([\d]+)([KMG]?) [\w]+.[\W\w]+[\w]+ +([\d.]+) +([\d]+)([KMG]?)([-+]?)""").toRegex()
                .find("top -l 2 -stats command,cpu,mem -pid $pid".runCommand().stdOutput)?.groupValues?.run {
                    whenNotNull(
                        getOrNull(5)?.toDoubleOrNull(), getOrNull(6),
                        getOrNull(7)?.toDoubleOrNull(), getOrNull(8)?.toIntOrNull(), getOrNull(9)
                    ) { unused, unusedUnit, cpu, mem, memUnit ->
                        totalMem = "sysctl hw.memsize".runCommand().stdOutput.split(" ")[1].toDouble()
                        availMem = unused * unusedUnit.unitFactor
                        overallUsedMem = totalMem - availMem
                        usedMem = mem * memUnit.unitFactor.toDouble()
                        memPercent = overallUsedMem / totalMem
                        cpuPercent = cpu / (Runtime.getRuntime().availableProcessors()) / 100
                    }
                }
            LINUX -> ("""[\w ]+: +([\d,.]+)[\w ]+, +([\d,.]+)[\w ]+, +([\d,.]+)[\w ]+, +([\d,.]+)[\w ]+\/[\w ]+\n[\w ]+: +([\d,.]+)[\w ]+, +([\d,.]+)[\w ]+, +([\d,.]+)[\w ]+. +([\d,.]+)[\w ]+[\W\w]+\d+ \d+ +\d+ +\d+ ++([\d,.]+)([kmg]) +([\d,.]+)([kmg]) +([\d,.]+)([kmg]) +\w +([\d,.]+) +([\d,.]+) +""").toRegex()
                .find("top -n 1 -p $pid".runCommand().stdOutput)?.groupValues?.run {
                    whenNotNull(
                        getOrNull(1)?.toDoubleOrNull(),
                        getOrNull(8)?.toDoubleOrNull(),
                        getOrNull(11)?.toDoubleOrNull(),
                        getOrNull(12),
                        getOrNull(15)?.toDoubleOrNull()
                    ) { total, avail, usedByProcess, usedByProcessUnit, cpu ->
                        totalMem = total * 1024 * 1024 * 1024
                        usedMem = usedByProcess * usedByProcessUnit.unitFactor
                        availMem = avail * 1024 * 1024 * 1024
                        memPercent = usedMem / totalMem
                        cpuPercent = cpu / Runtime.getRuntime().availableProcessors() / 100
                    }
                }
            else -> Unit
        }
    }

    private val String.unitFactor
        get() = when (toLowerCase(Locale.getDefault())) {
            "g" -> 1024 * 1024 * 1024
            "m" -> 1024 * 1024
            "k" -> 1024
            else -> 1
        }

    fun release() = autoUpdateJob?.cancel().asUnit()
}

fun main() = runBlocking {
    console {
        outnl(*SysInfo())
        delay(60.seconds)
    }
}

@Suppress("FunctionName")
fun CoroutineScope.SysInfo(
    autoUpdateInterval: IDurationEx? = 1.seconds,
    onUpdate: TopInfo.() -> Unit = { refresh() }
): Array<TextWithColor> {
    val top = TopInfo(scope = this, autoUpdateInterval = autoUpdateInterval, onUpdate = onUpdate)
    return PROGRESS(Text {
        done = ((top.cpuPercent * 1000).toLong())
        total = 1000
        "CPU "
    }, Bar(size = 17, foreground = PROGRESS_COLORS.reversed())) + PROGRESS(Text {
        done = top.usedMem.toLong()
        total = top.run { usedMem + availMem }.toLong()
        " - MEM "
    }, Bar(size = 17, foreground = PROGRESS_COLORS.reversed()), Space, DoneOfTotal(), unit = ProgressUnit.Bytes)
}

class SystemInfoComponent(
    definition: ConsoleDefinition,
    screen: Screen,
    scope: CoroutineScope
) : AbsConsoleComponent<SystemInfoComponent>(definition, screen, scope) {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) = runBlocking {
            console(ConsoleDefinition(0.0.rel to 0.0.rel, 1.0.rel to 1.0.rel, false) { scr, s, _, d ->
                SystemInfoComponent(d, scr, s)
            }) { }
        }.asUnit()
    }

    private val top = TopInfo(scope = scope, autoUpdateInterval = 1.seconds, onUpdate = { invalidate() })

    override fun drawComponent(graphics: TextGUIGraphics, component: SystemInfoComponent) {
        graphics.putText(0, 0, { invalidate() }, {}, *PROGRESS(Text {
            done = ((top.cpuPercent * 1000).toLong())
            total = 1000
            "CPU "
        }, Bar(size = 17, foreground = PROGRESS_COLORS.reversed())) + PROGRESS(Text {
            done = top.usedMem.toLong()
            total = top.run { usedMem + availMem }.toLong()
            " - MEM "
        }, Bar(size = 17, foreground = PROGRESS_COLORS.reversed()), Space, DoneOfTotal(), unit = ProgressUnit.Bytes))
    }
}