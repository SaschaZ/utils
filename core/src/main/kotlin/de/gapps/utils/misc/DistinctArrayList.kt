package de.gapps.utils.misc

class DistinctArrayList<T> private constructor(private val e: MutableList<T>) : MutableList<T> by e {

    constructor() : this(ArrayList())

    override fun add(element: T) = if (!e.contains(element)) e.add(element) else false

    override fun add(index: Int, element: T) = if (!e.contains(element)) e.add(index, element) else Unit

    override fun addAll(index: Int, elements: Collection<T>) = e.addAll(index, elements.filter { !e.contains(it) })

    override fun addAll(elements: Collection<T>) = e.addAll(elements.size, elements)

    override fun set(index: Int, element: T) = e.firstOrNull { it == element } ?: e.set(index, element)

    operator fun plus(element: T) = apply { add(element) }

    operator fun plusAssign(element: T) = add(element).asUnit()

    operator fun plus(elements: Collection<T>) = apply { addAll(elements) }

    operator fun plusAssign(elements: Collection<T>) = addAll(elements).asUnit()
}