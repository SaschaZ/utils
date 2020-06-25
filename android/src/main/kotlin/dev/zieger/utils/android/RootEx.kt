package dev.zieger.utils.android

import dev.zieger.utils.coroutines.runCommand
import java.io.InputStream

suspend fun root(block: suspend (inStr: InputStream, errStr: InputStream) -> Unit) {
    "su".runCommand(block = block)
}
