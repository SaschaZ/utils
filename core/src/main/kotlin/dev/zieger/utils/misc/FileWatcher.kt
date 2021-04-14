@file:Suppress("unused")

package dev.zieger.utils.misc

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.misc.FileHelper.files
import dev.zieger.utils.time.delay
import dev.zieger.utils.time.duration.IDurationEx
import dev.zieger.utils.time.duration.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import java.io.File

fun File.watch(
    scope: CoroutineScope,
    interval: IDurationEx = 10.seconds,
    exclude: Regex? = null,
    listener: suspend (removed: List<File>, added: List<File>) -> Boolean
): Job = scope.launchEx { watch(interval, exclude, listener) }

suspend fun File.watch(
    interval: IDurationEx = 10.seconds,
    exclude: Regex? = null,
    listener: suspend (removed: List<File>, added: List<File>) -> Boolean
) {
    var active = true
    var prevFiles = files
    while (active) {
        val curFiles = files.filterNot { exclude?.matches(it.absolutePath) == true }
        val removed = prevFiles - curFiles
        val added = curFiles - prevFiles

        active = listener(removed, added)

        prevFiles = curFiles

        delay(interval)
    }
}