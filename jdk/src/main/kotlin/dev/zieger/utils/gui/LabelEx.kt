package dev.zieger.utils.gui

import com.googlecode.lanterna.gui2.Label

@Suppress("FunctionName")
fun <M> IModelHolder<M>.LabelEx(text: String = ""): LabelEx<M> = LabelEx(this) { text }

@Suppress("FunctionName")
fun <M> IModelHolder<M>.LabelEx(textProvider: suspend M.() -> Any?): LabelEx<M> = LabelEx(this, textProvider)

open class LabelEx<M>(
    modelHolder: IModelHolder<M>,
    private val textProvider: suspend M.() -> Any?
) : Label(""), IModelUpdate<M>, IModelHolder<M> by modelHolder, IGridLayoutElement by GridLayoutElement() {

    init {
        this.text = text
    }

    override suspend fun updateModel(block: M.() -> M) {
        text = "${model.block().textProvider()}"
    }
}