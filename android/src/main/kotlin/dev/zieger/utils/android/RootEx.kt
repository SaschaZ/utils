package dev.zieger.utils.android

import dev.zieger.utils.coroutines.runCommand
import java.io.InputStream

suspend fun root(block: (inStr: InputStream, errStr: InputStream) -> Unit) {
    "su".runCommand(block = block)
}
