package dev.zieger.utils.gui

import com.googlecode.lanterna.gui2.Component
import com.googlecode.lanterna.gui2.GridLayout
import dev.zieger.utils.misc.whenNotNull

class GridLayoutElement :
    IGridLayoutElement {

    override var horizontalAlign: GridLayout.Alignment =
        GridLayout.Alignment.BEGINNING
    override var verticalAlign: GridLayout.Alignment =
        GridLayout.Alignment.BEGINNING

    override var grabHorizontalMoreSpace = false
    override var grabVerticalMoreSpace = false

    override var horizontalSpan: Int = 1
    override var verticalSpan: Int = 1

    override fun updateLayoutData(component: Component) {
        component.layoutData = GridLayout.createLayoutData(
            horizontalAlign, verticalAlign, grabHorizontalMoreSpace, grabVerticalMoreSpace, horizontalSpan, verticalSpan
        )
    }
}


@Suppress("UNCHECKED_CAST")
interface IGridLayoutElement {

    var horizontalAlign: GridLayout.Alignment
    var verticalAlign: GridLayout.Alignment

    var grabHorizontalMoreSpace: Boolean
    var grabVerticalMoreSpace: Boolean

    var horizontalSpan: Int
    var verticalSpan: Int

    fun updateLayoutData(component: Component)
}

val <T> T.rightAlign: T
    get() = config { horizontalAlign = GridLayout.Alignment.END; updateLayoutData(it) }
val <T> T.leftAlign: T
    get() = config { horizontalAlign = GridLayout.Alignment.BEGINNING; updateLayoutData(it) }
val <T> T.horizontalCenterAlign: T
    get() = config { horizontalAlign = GridLayout.Alignment.CENTER; updateLayoutData(it) }
val <T> T.horizontalFillAlign: T
    get() = config { horizontalAlign = GridLayout.Alignment.FILL; updateLayoutData(it) }
val <T> T.grabHorizontalMoreSpaceOn: T
    get() = config { grabHorizontalMoreSpace = true; updateLayoutData(it) }
val <T> T.grabHorizontalMoreSpaceOff: T
    get() = config { grabHorizontalMoreSpace = false; updateLayoutData(it) }
val <T> T.incHorizontalSpan: T
    get() = config { horizontalSpan++; updateLayoutData(it) }

val <T> T.bottomAlign: T
    get() = config { c -> verticalAlign = GridLayout.Alignment.END; updateLayoutData(c) }
val <T> T.topAlign: T
    get() = config { verticalAlign = GridLayout.Alignment.BEGINNING; updateLayoutData(it) }
val <T> T.verticalCenterAlign: T
    get() = config { verticalAlign = GridLayout.Alignment.CENTER; updateLayoutData(it) }
val <T> T.verticalFillAlign: T
    get() = config { verticalAlign = GridLayout.Alignment.FILL; updateLayoutData(it) }
val <T> T.grabVerticalMoreSpaceOn: T
    get() = config { grabVerticalMoreSpace = true; updateLayoutData(it) }
val <T> T.grabVerticalMoreSpaceOff: T
    get() = config { grabVerticalMoreSpace = false; updateLayoutData(it) }
val <T> T.incVerticalSpan: T
    get() = config { verticalSpan++; updateLayoutData(it) }

fun <T> T.config(block: IGridLayoutElement.(Component) -> Unit): T = apply {
    @Suppress("UNCHECKED_CAST")
    whenNotNull(this as? Component, this as? IGridLayoutElement) { c, g -> block(g, c) }
}