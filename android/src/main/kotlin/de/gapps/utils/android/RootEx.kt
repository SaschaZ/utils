package de.gapps.utils.android

import de.gapps.utils.coroutines.runCommand

suspend fun root(block: () -> Unit) {
    "su".runCommand(block = block)
}
