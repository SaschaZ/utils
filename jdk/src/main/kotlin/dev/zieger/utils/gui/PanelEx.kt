package dev.zieger.utils.gui

import com.googlecode.lanterna.gui2.*
import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.DefaultCoroutineScope
import dev.zieger.utils.delegates.OnChanged
import dev.zieger.utils.misc.asUnit
import dev.zieger.utils.misc.runEach
import kotlinx.coroutines.CoroutineScope

class SimpleModelHolder<M>(override var model: M) : IModelHolder<M>

open class PanelEx<M>(
    modelHandler: IModelHolder<M>,
    numColumns: Int = 2,
    layoutManager: LayoutManager = GridLayout(numColumns),
    scope: CoroutineScope = DefaultCoroutineScope()
) : Panel(layoutManager), IComponentUpdate<M>, IModelUpdate<M> {

    constructor(
        model: M, numColumns: Int = 2,
        layoutManager: LayoutManager = GridLayout(numColumns),
        scope: CoroutineScope = DefaultCoroutineScope()
    ) : this(SimpleModelHolder(model), numColumns, layoutManager, scope)

    var components: MutableList<Component> = ArrayList()

    override var model: M by OnChanged(
        modelHandler.model,
        notifyOnChangedValueOnly = false,
        notifyForInitial = true
    ) { model ->
        scope.launchEx { components.forEach { component -> component.updateModelInternal(model) } }
    }

    override fun addComponent(component: Component?): Panel = addComponent(components.size, component)

    override fun addComponent(index: Int, component: Component?): Panel {
        component?.also { components.add(index, it) }
        return super.addComponent(index, component)
    }

    override suspend fun updateComponents(block: List<Component>.() -> List<Component>) {
        val newComponents = components.block()
        removeAllComponents()
        components.clear()
        newComponents.forEach { addComponent(it) }
        components.runEach { updateModelInternal(model) }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun Component.updateModelInternal(model: M): Unit = when (this) {
        is Container -> childrenList.forEach { it.updateModelInternal(model) }.asUnit()
        is IModelUpdate<*> -> (this as IModelUpdate<M>).updateModel { model }
        else -> Unit
    }
}

interface IComponentUpdate<M> {

    suspend fun updateComponents(block: List<Component>.() -> List<Component>)
}

interface IModelUpdate<M> : IModelHolder<M> {

    suspend fun updateModel(block: M.() -> M = { this }) {
        model = model.block()
    }
}

interface IModelHolder<M> {

    var model: M
}

