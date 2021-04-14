@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package dev.zieger.utils.misc

import dev.zieger.utils.time.ITimeEx
import dev.zieger.utils.time.base.minus
import dev.zieger.utils.time.duration.days
import dev.zieger.utils.time.progression.rangeBase
import dev.zieger.utils.time.string.DateFormat
import dev.zieger.utils.time.string.GMT
import dev.zieger.utils.time.string.parse
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.streams.toList

@Suppress("NewApi")
object FileHelper {

    val String.files: List<File> get() = Paths.get(this).files.map { it.toFile() }

    val File.files: List<File>
        get() = when {
            isDirectory -> listFiles()?.flatMap { it.files } ?: emptyList()
            else -> listOf(this)
        }

    val Path.files: List<Path>
        get() = (Files.list(this)?.toList()?.flatMap { file ->
            if (Files.isDirectory(file)) file.files
            else listOf(file)
        } ?: emptyList()).filter { Files.exists(it) }

    val File.dateTime: ITimeEx
        get() = path.split(File.separatorChar).let { path ->
            catch(null, printStackTrace = false) {
                path.last().split(".").run {
                    ("1.${(get(1).toInt()).let { if (it == 12) 1 else it + 1 }}." +
                            get(0).let { if (get(1).toInt() == 12) it + 1 else it }).parse()
                }
            } ?: catch(null, printStackTrace = false) {
                path.last().split(".").run { "1.1.${get(0)}".parse() }
            } ?: "${path.last(1)}T${path.last().removeSuffix(".json")}:00:00".parse(GMT)
        }

    private val <T : ITimeEx> T.firstLevelDirectoryName: String
        get() = rangeBase(1.days).run { this - (dayOfMonth - 1).days }.formatTime(DateFormat.FILENAME_DATE) + "/"

    private val <T : ITimeEx> T.secondLevelDirectoryName: String
        get() = firstLevelDirectoryName + formatTime(DateFormat.FILENAME_DATE) + "/"

    val <T : ITimeEx> T.fileName: String
        get() = secondLevelDirectoryName + "%02d".format(hourOfDay) + ".json"


    val <T : ITimeEx> List<T>.fileName: String get() = first().fileName

    val File.size get() = if (!exists() || isDirectory) 0
    else Paths.get(absolutePath).let { if (Files.isDirectory(it)) 0 else Files.size(it) }

    val List<File>.totalSize: Long get() = sumByLong { it.size }
}