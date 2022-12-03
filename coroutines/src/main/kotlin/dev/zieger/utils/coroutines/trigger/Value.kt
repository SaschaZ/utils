package dev.zieger.utils.coroutines.trigger

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


interface Observable {
    suspend fun values(block: suspend () -> Unit): suspend () -> Unit
}

interface IValue<T> : Observable {
    val value: T
    suspend fun value(value: T)
}

open class Value<T>(
    initial: T,
    vararg depends: Observable,
    private val printDebug: Boolean = false,
    private val debugName: String? = null,
    block: (suspend Value<T>.() -> Unit)? = null
) : IValue<T> {

    init {
        if (depends.isEmpty() && block != null)
            throw IllegalArgumentException("No dependents set")

        runBlocking {
            depends.forEach {
                it.values { block!!() }
            }
        }
    }

    override var value: T = initial
        protected set

    private val valueMutex = Mutex()

    private val observer = LinkedList<suspend () -> Unit>()
    private val observerMutex = Mutex()

    private val name
        get() = "${debugName ?: "value"}(#${System.identityHashCode(this)})"

    override suspend fun value(value: T) = valueMutex.withLock {
        val obs = ArrayList(observer)
        if (printDebug)
            println("$name changing from ${this.value} to $value (will notify ${obs.size} observer)")
        this.value = value
        obs.forEach { it() }
    }

    override suspend fun values(
        block: suspend () -> Unit
    ): suspend () -> Unit = observerMutex.withLock {
        observer += block
        return {
            observerMutex.withLock { observer -= block }
        }
    }
}

suspend fun <T> IValue<T>.nextValue(): T {
    val next = Channel<T>()
    val remove = values { next.send(value) }
    return next.receive().also { remove() }
}

fun <T> value(
    initial: T,
    vararg depends: Observable,
    printDebug: Boolean = false,
    block: (suspend Value<T>.() -> Unit)? = null
) = object : ReadOnlyProperty<Any, Value<T>> {

    private var value: Value<T>? = null

    override fun getValue(thisRef: Any, property: KProperty<*>) =
        value ?: Value<T>(
            initial, *depends,
            printDebug = printDebug, debugName = property.name, block = block
        ).also { value = it }
}
