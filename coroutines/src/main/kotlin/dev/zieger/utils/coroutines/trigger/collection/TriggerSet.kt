package dev.zieger.utils.coroutines.trigger.collection

import dev.zieger.utils.coroutines.scope.ScopeHolder
import dev.zieger.utils.coroutines.trigger.Observable
import dev.zieger.utils.coroutines.trigger.Trigger
import kotlinx.coroutines.CoroutineScope
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T> ScopeHolder.triggerSet(
    vararg depends: Observable,
    innerSet: MutableSet<T> = HashSet(),
    triggerSelfChange: Boolean = false,
    printDebug: Boolean = false,
    block: (suspend TriggerSet<T>.() -> Unit)? = null
) = object : ReadOnlyProperty<ScopeHolder, TriggerSet<T>> {
    private var value: TriggerSet<T>? = null

    override fun getValue(thisRef: ScopeHolder, property: KProperty<*>): TriggerSet<T> =
        value ?: TriggerSet<T>(
            scope, innerSet, *depends,
            triggerSelfChange = triggerSelfChange,
            printDebug = printDebug, debugName = property.name, block = block
        ).also { value = it }
}

open class TriggerSet<T>(
    scope: CoroutineScope,
    private val innerSet: MutableSet<T> = HashSet(),
    vararg depends: Observable,
    private val triggerSelfChange: Boolean = false,
    printDebug: Boolean = false,
    debugName: String? = null,
    private val block: (suspend TriggerSet<T>.() -> Unit)? = null
) : MutableSet<T> by innerSet, Trigger(
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
        val r = innerSet.add(element)
        trigger()
        return r
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val r = innerSet.addAll(elements)
        trigger()
        return r
    }

    override fun clear() {
        innerSet.clear()
        trigger()
    }

    override fun remove(element: T): Boolean {
        val r = innerSet.remove(element)
        trigger()
        return r
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val r = innerSet.removeAll(elements.toSet())
        trigger()
        return r
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val r = innerSet.retainAll(elements.toSet())
        trigger()
        return r
    }
}