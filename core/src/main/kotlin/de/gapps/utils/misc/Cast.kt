package de.gapps.utils.misc

@Suppress("UNCHECKED_CAST")
fun <T> Any.cast() = this as T

@Suppress("UNCHECKED_CAST")
fun <T> Any.castSafe() = this as? T