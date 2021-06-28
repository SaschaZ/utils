package dev.zieger.utils.misc

inline fun <reified T: Any> Any.cast(): T = this as T

fun Any.asUnit(): Unit = Unit