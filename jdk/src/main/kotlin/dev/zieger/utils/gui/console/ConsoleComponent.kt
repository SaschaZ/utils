//package dev.zieger.utils.gui.console
//
//import com.googlecode.lanterna.TerminalPosition
//import com.googlecode.lanterna.TerminalSize
//import com.googlecode.lanterna.gui2.*
//import com.googlecode.lanterna.screen.Screen
//import kotlinx.coroutines.CoroutineScope
//
//open class ConsoleComponent(
//    screen: Screen,
//    termPosition: TerminalPosition = TerminalPosition.TOP_LEFT_CORNER,
//    termSize: TerminalSize = TerminalSize.ONE
//) : AbstractInteractableComponent<ConsoleComponent>() {
//
//    companion object {
//
//        @JvmStatic
//        fun main(args: Array<String>) {
//            val term = object : Term() {
//                override suspend fun onCreate(window: Window) {
//                    super.onCreate(window)
//                    val panel = Panel()
//                    panel.addComponent(
//                        ConsoleComponent(
//                            screen,
//                            TerminalPosition(1, 1), TerminalSize(10, 10)
//                        ).withBorder(Borders.doubleLineBevel())
//                    )
//                    window.component = panel
//                }
//
//                override suspend fun onStart(scope: CoroutineScope) {
//                    super.onStart(scope)
//                }
//            }
//            term.start()
//        }
//    }
//
//    internal val console = LanternaConsole(screen, termPosition, termSize, isStandalone = false)
//
//    override fun createDefaultRenderer(): InteractableRenderer<ConsoleComponent> = ConsoleRenderer()
//}
//
//open class ConsoleRenderer : InteractableRenderer<ConsoleComponent> {
//
//    override fun getPreferredSize(component: ConsoleComponent): TerminalSize = component.console.preferredSize!!
//    override fun getCursorLocation(component: ConsoleComponent): TerminalPosition =
//        TerminalPosition(3, component.console.preferredSize!!.rows)
//
//    override fun drawComponent(graphics: TextGUIGraphics, component: ConsoleComponent) =
//        component.console.draw(graphics)
//}