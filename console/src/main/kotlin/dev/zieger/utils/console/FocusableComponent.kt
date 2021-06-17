package dev.zieger.utils.console

import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.input.KeyStroke
import kotlinx.coroutines.CoroutineScope

interface FocusableComponent {

    var hasFocus: Boolean
    suspend fun onKeyPressed(keyStroke: KeyStroke)
}

interface FocusableConsoleComponent : Component, FocusableComponent, ConsoleScope {
    var options: ConsoleOptions
    var scope: CoroutineScope?
}