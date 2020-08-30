package dev.zieger.utils.gui.console

import com.googlecode.lanterna.gui2.*
import dev.zieger.utils.misc.FiFo

open class Console : Term() {

    companion object {

        private const val BUFFER_SIZE = 500

        @JvmStatic
        fun main(args: Array<String>) {
            Console().start()
        }
    }

    private val panel = Panel(LinearLayout(Direction.VERTICAL))
    private val messages = FiFo<String>(BUFFER_SIZE)

    override suspend fun onCreate(window: Window) {
        window.component = panel.apply {
            repeat(BUFFER_SIZE) {
                messages.put("fooboo $it")
            }
            update()
            terminal.addResizeListener { _, _ -> update() }
        }
    }

    private fun Panel.update() {
        removeAllComponents()
        (messages.fittingRows downTo 0).forEach {
            val str = messages[messages.lastIndex - it]
            addComponent(Label(str))
        }
    }

    private val List<String>.fittingRows: Int
        get() {
            var rows = 0
            reversed().forEach {
                val itemRows = it.rows
                if (rows + itemRows >= terminal.terminalSize.rows - 2) return@forEach
                rows += itemRows
            }
            return rows
        }

    private val String.rows: Int
        get() {
            val maxColumns = terminal.terminalSize.columns - 2
            var rows = 1
            var cols = 0
            forEach { c ->
                when {
                    c == '\n' -> {
                        rows++
                        cols = 0
                    }
                    c == '\t' -> {
                        cols += 8 - cols % 8
                    }
                    cols == maxColumns -> {
                        rows++
                        cols = 1
                    }
                    else -> cols++
                }
            }
            return rows
        }
}