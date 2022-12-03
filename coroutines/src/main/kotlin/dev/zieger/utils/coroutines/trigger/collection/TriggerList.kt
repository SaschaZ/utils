package dev.zieger.utils.coroutines.trigger.collection

import dev.zieger.utils.coroutines.scope.ScopeHolder
import dev.zieger.utils.coroutines.trigger.Observable
import dev.zieger.utils.coroutines.trigger.Trigger
import kotlinx.coroutines.CoroutineScope
import java.util.*
import java.util.function.UnaryOperator
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T> ScopeHolder.triggerList(
    vararg depends: Observable,
    innerList: MutableList<T> = LinkedList(),
    triggerSelfChange: Boolean = false,
    printDebug: Boolean = false,
    block: (suspend TriggerList<T>.() -> Unit)? = null
) = object : ReadOnlyProperty<ScopeHolder, TriggerList<T>> {
    private var value: TriggerList<T>? = null

    override fun getValue(thisRef: ScopeHolder, property: KProperty<*>): TriggerList<T> =
        value ?: TriggerList<T>(
            scope, innerList, *depends,
            triggerSelfChange = triggerSelfChange,
            printDebug = printDebug, debugName = property.name, block = block
        ).also { value = it }
}

open class TriggerList<T>(
    scope: CoroutineScope,
    private val innerList: MutableList<T> = LinkedList(),
    vararg depends: Observable,
    private val triggerSelfChange: Boolean = false,
    printDebug: Boolean = false,
    debugName: String? = null,
    private val block: (suspend TriggerList<T>.() -> Unit)? = null
) : MutableList<T> by innerList, Trigger(
    scope, *depends,
    printDebug = printDebug, debugName = debugName
) {

    init {
        if (depends.isEmpty() && block != null)
            throw IllegalArgumentException("No dependents set")
    }

    override suspend fun dependentChanged() = block!!()

    override suspend fun triggerInternal() {
        if (triggerSelfChange) block?.invoke(this)
        super.triggerInternal()
    }

    override fun add(element: T): Boolean {
        val result = innerList.add(element)
        trigger()
        return result
    }

    override fun add(index: Int, element: T) {
        innerList.add(index, element)
        trigger()
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        val r = innerList.addAll(index, elements)
        trigger()
        return r
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val result = innerList.addAll(elements)
        trigger()
        return result
    }

    override fun removeAt(index: Int): T {
        val r = innerList.removeAt(index)
        trigger()
        return r
    }

    override fun replaceAll(operator: UnaryOperator<T>) {
        val r = innerList.replaceAll(operator)
        trigger()
        return r
    }

    override fun set(index: Int, element: T): T {
        val r = innerList.set(index, element)
        trigger()
        return r
    }
}