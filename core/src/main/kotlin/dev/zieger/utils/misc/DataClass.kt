package dev.zieger.utils.misc

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

open class DataClass {

    override fun equals(other: Any?): Boolean {
        return (other as? DataClass)?.properties()?.mapIndexed { index, t ->
            properties().getOrNull(index)?.getter?.call(this)?.compare(t.getter.call(other)) != false
        }?.all { it } == true
    }

    private fun Any?.compare(other: Any?): Boolean {
        val result = when {
            this is Collection<Any?> && other is Collection<Any?> -> {
                val results = ArrayList<Boolean>()
                if (size == other.size) {
                    val iter0 = iterator()
                    val iter1 = other.iterator()
                    while (iter0.hasNext() && iter1.hasNext())
                        results.add(iter0.next() == iter1.next())
                }
                return results.size == size && size == other.size && results.all { it }
            }
            else -> this == other
        }
        println("compare(): $this => $result")
        return result
    }

    override fun hashCode(): Int {
        val properties = properties()
        val result = super.hashCode() + properties.sumBy { it.hashCode() }
        println("hashCode() $this -> $result")
        return result
    }

    override fun toString(): String = "${this::class.name}(" +
            "${properties().map { it.name to it.getter.call(this) }
                .filter { (it.second as? Collection<*>)?.isEmpty() != true }
                .joinToString("; ") { "${it.first}=${it.second}" }})"
}

inline fun <reified E : DataClass> E.properties(): List<KProperty1<E, *>> = E::class.memberProperties.toList()
