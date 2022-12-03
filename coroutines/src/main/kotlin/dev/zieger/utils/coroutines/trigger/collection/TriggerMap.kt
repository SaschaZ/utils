package dev.zieger.utils.coroutines.trigger.collection

import dev.zieger.utils.coroutines.builder.launchEx
import dev.zieger.utils.coroutines.scope.ScopeHolder
import dev.zieger.utils.coroutines.trigger.Observable
import dev.zieger.utils.coroutines.trigger.Trigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <K, V> ScopeHolder.triggerMap(
    vararg depends: Observable,
    innerMap: MutableMap<K, V> = HashMap(),
    triggerSelfChange: Boolean = false,
    printDebug: Boolean = false,
    block: (suspend TriggerMap<K, V>.() -> Unit)? = null
) = object : ReadOnlyProperty<ScopeHolder, TriggerMap<K, V>> {
    private var value: TriggerMap<K, V>? = null

    override fun getValue(thisRef: ScopeHolder, property: KProperty<*>): TriggerMap<K, V> =
        value ?: TriggerMap<K, V>(
            scope, innerMap, *depends,
            triggerSelfChange = triggerSelfChange,
            printDebug = printDebug, debugName = property.name, block = block
        ).also { value = it }
}

open class TriggerMap<K, V>(
    scope: CoroutineScope,
    private val innerMap: MutableMap<K, V> = HashMap(),
    vararg depends: Observable,
    private val triggerSelfChange: Boolean = false,
    printDebug: Boolean = false,
    debugName: String? = null,
    private val block: (suspend TriggerMap<K, V>.() -> Unit)? = null
) : MutableMap<K, V> by innerMap, Trigger(
    scope, *depends,
    printDebug = printDebug, debugName = debugName
) {

    init {
        if (depends.isEmpty() && block != null)
            throw IllegalArgumentException("No dependents set")
    }

    override val entries = TriggerSet<MutableMap.MutableEntry<K, V>>(scope)
    override val keys = TriggerSet<K>(scope)
    override val values = TriggerCollection<V>(scope)

    override suspend fun dependentChanged() = block!!()

    override suspend fun triggerInternal() {
        if (triggerSelfChange) block?.invoke(this)
        super.triggerInternal()
    }

    override suspend fun values(block: suspend () -> Unit): suspend () -> Unit {
        val entriesRemove = entries.values(block)
        val keysRemove = keys.values(block)
        val valuesRemove = values.values(block)
        return {
            entriesRemove()
            keysRemove()
            valuesRemove()
        }
    }

    override fun clear() {
        innerMap.clear()
        trigger()
    }

    override fun put(key: K, value: V): V? {
        val r = innerMap.put(key, value)
        trigger()
        return r
    }

    override fun putAll(from: Map<out K, V>) {
        val r = innerMap.putAll(from)
        trigger()
        return r
    }

    override fun remove(key: K): V? {
        val r = innerMap.remove(key)
        trigger()
        return r
    }
}

suspend fun suspendUntilFirstResumes(vararg block: suspend () -> Unit) {
    val result = Channel<Unit>()
    val jobs = block.map { b ->
        launchEx {
            b()
            if (isActive)
                result.send(Unit)
        }
    }
    result.receive()
    jobs.cancelAll()
}

fun <T : Job> List<T>.cancelAll() = forEach { it.cancel() }