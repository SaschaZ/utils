package dev.zieger.utils.misc

inline fun <reified T : Any> Any.cast(): T = this as T

inline fun <reified T : Any> Any.castSafe(): T? = this as? T

fun Any.asUnit(): Unit = Unit