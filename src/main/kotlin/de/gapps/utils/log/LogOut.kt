package de.gapps.utils.log

import java.io.OutputStream
import java.io.PrintStream

internal object LogOut {

    internal val out = object : OutputStream() {

        private var sb = StringBuilder()

        private var previousCharWasNl = true

        override fun write(p0: Int) {
            when {
                previousCharWasNl -> {
                    printPrefix()
                    previousCharWasNl = false
                }
            }
            when (val c = p0.toChar()) {
                '\n' -> {
                    previousCharWasNl = true
                    flush()
                }
                else -> sb.append(c)
            }
        }

        override fun flush() {
            origOut.write(sb.toString().toByteArray())
            sb.clear()
        }

        override fun close() = flush()
    }

    private fun printPrefix() {
        origOut.print(MessageBuilder.prefix)
    }

    val origOut: PrintStream = System.out

    init {
        System.setOut(PrintStream(out))
    }
}