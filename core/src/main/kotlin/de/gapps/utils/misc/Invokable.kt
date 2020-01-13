package de.gapps.utils.misc

interface Invokable<T> {

    operator fun invoke(): T
}

interface InvokableEnum : Invokable<Int> {

    override operator fun invoke(): Int = (this as? Enum<*>)?.ordinal ?: -1
}