package dev.zieger.utils.gui.console

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2.Interactable.Result
import com.googlecode.lanterna.gui2.TextGUIGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType.*
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.time.duration.milliseconds
import kotlinx.coroutines.CoroutineScope

class CommandComponent(
    definition: ConsoleDefinition,
    screen: PanelScreen,
    scope: CoroutineScope,
    private val attachedConsole: ConsoleComponent
) : AbsConsoleComponent<CommandComponent>(definition, screen, scope, true) {

    private val commandPrefix: TextWithColor get() = if (isFocused) definition.activeCommandPrefix else definition.commandPrefix
    private val availableCols get() = size.columns - commandPrefix.text(TextScope({}, {})).toString().length

    private var rawCommand by OnChanged("", notifyOnChangedValueOnly = false) {
        command = when {
            availableCols > 1 && it.length >= availableCols - 2 -> when {
                it.length + cursorDiff - availableCols / 2 <= 0 -> it.take(availableCols - 1) + "…"
                cursorDiff < -availableCols / 2 -> {
                    "…" + it.substring(
                        (it.length + cursorDiff - availableCols / 2 + 1).coerceAtLeast(0) until
                                (it.length + cursorDiff + availableCols / 2 - 1).coerceAtMost(it.lastIndex)
                    ) + "…"
                }
                else -> "…" + it.takeLast(availableCols - 2)
            }
            else -> it
        }
    }
    private var command: String by OnChanged("") { invalidate() }

    private var cursorDiff: Int by OnChanged(0, map = {
        it.coerceIn(-rawCommand.length..0)
    }) {
        rawCommand = rawCommand
    }

    private var cursorVisible by OnChanged(isFocused) {
        invalidate()
    }

    init {
        scope.launchEx(interval = 666.milliseconds) { cursorVisible = !cursorVisible }
    }

    override fun handleKeyStroke(keyStroke: KeyStroke): Result = keyStroke.run {
//        Log.v("keyStroke=$keyStroke")
        when {
            !isAltDown && !isCtrlDown -> {
                when (keyType) {
                    ArrowUp -> {
                        attachedConsole.scroll(5)
                        Result.HANDLED
                    }
                    ArrowDown -> {
                        attachedConsole.scroll(-5)
                        Result.HANDLED
                    }
                    ArrowLeft -> {
                        cursorDiff--
                        Result.HANDLED
                    }
                    ArrowRight -> {
                        cursorDiff++
                        Result.HANDLED
                    }
                    Backspace -> {
                        rawCommand = rawCommand.removeRange((rawCommand.length + cursorDiff - 1).let { it..it })
                        Result.HANDLED
                    }
                    Enter -> {
                        if (rawCommand.isNotBlank()) {
                            attachedConsole.newMessage(Message(text(rawCommand), true))
                            rawCommand = ""
                            cursorDiff = 0
                        }
                        Result.HANDLED
                    }
                    else -> {
                        character?.let {
                            if (isShiftDown) it.toUpperCase() else it
                        }?.also { c ->
                            rawCommand = rawCommand.take(rawCommand.length + cursorDiff) + c +
                                    rawCommand.takeLast(-cursorDiff)
                        }
                        Result.HANDLED
                    }
                }
            }
            isAltDown -> when (keyType) {
                ArrowLeft -> Result.MOVE_FOCUS_PREVIOUS
                ArrowRight -> Result.MOVE_FOCUS_NEXT
                ArrowUp -> Result.MOVE_FOCUS_UP
                ArrowDown -> Result.MOVE_FOCUS_DOWN
                else -> Result.UNHANDLED
            }
            else -> Result.UNHANDLED
        }
    }

    override fun getCursorLocation(component: CommandComponent): TerminalPosition? =
        if (isFocused && cursorVisible) {
            val prefixLength = "${commandPrefix.text(TextScope({}, {}))}".length
            val available = size.columns - prefixLength
            val cursorPos = when {
                cursorDiff == 0 -> command.length
                cursorDiff == -rawCommand.length -> 0
                cursorDiff <= -rawCommand.length + available / 2 -> rawCommand.length + cursorDiff
                cursorDiff <= -available / 2 -> available / 2
                else -> command.length + cursorDiff
            }
            TerminalPosition(prefixLength + cursorPos, 0)
        } else null

    override fun applySize(newSize: TerminalSize) {
        position = definition.commandPosition(newSize)
        size = definition.commandSize(newSize)
    }

    override fun drawComponent(graphics: TextGUIGraphics?, component: CommandComponent) =
        graphics?.putText(
            0, 0, { invalidate() }, {}, commandPrefix, text(command)
        ).asUnit()
}