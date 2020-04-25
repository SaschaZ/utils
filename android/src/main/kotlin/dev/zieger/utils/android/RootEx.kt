package dev.zieger.utils.android

import dev.zieger.utils.coroutines.runCommand

suspend fun root(block: () -> Unit) {
    "su".runCommand(block = block)
}
