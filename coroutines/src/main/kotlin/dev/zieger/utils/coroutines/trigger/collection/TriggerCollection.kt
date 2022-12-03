package dev.zieger.utils.coroutines.trigger.collection

import dev.zieger.utils.coroutines.scope.ScopeHolder
import dev.zieger.utils.coroutines.trigger.Observable
import dev.zieger.utils.coroutines.trigger.Trigger
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T> ScopeHolder.triggerCollection(
    vararg depends: Observable,
    innerCollection: MutableList<T> = LinkedList(),
    triggerSelfChange: Boolean = false,
    printDebug: Boolean = false,
    block: (suspend TriggerCollection<T>.() -> Unit)? = null
) = object : ReadOnlyProperty<ScopeHolder, TriggerCollection<T>> {
    private var value: TriggerCollection<T>? = null

    override fun getValue(thisRef: ScopeHolder, property: KProperty<*>): TriggerCollection<T> =
        value ?: TriggerCollection<T>(
            scope, innerCollection, *depends,
            triggerSelfChange = triggerSelfChange,
            printDebug = printDebug, debugName = property.name, block = block
        ).also { value = it }
}

open class TriggerCollection<T>(
    scope: CoroutineScope,
    private val innerCollection: MutableCollection<T> = LinkedList(),
    vararg depends: Observable,
    private val triggerSelfChange: Boolean = false,
    private val printDebug: Boolean = false,
    private val debugName: String? = null,
    private val block: (suspend TriggerCollection<T>.() -> Unit)? = null
) : MutableCollection<T> by innerCollection, Trigger(scope) {

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
        val r = innerCollection.add(element)
        trigger()
        return r
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val r = innerCollection.addAll(elements)
        trigger()
        return r
    }

    override fun clear() {
        innerCollection.clear()
        trigger()
    }

    override fun remove(element: T): Boolean {
        val r = innerCollection.remove(element)
        trigger()
        return r
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        val r = innerCollection.removeAll(elements)
        trigger()
        return r
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        val r = innerCollection.retainAll(elements)
        trigger()
        return r
    }
}