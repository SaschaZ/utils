package dev.zieger.utils.misc

fun <T> T.anyOf(vararg values: T): Boolean = values.contains(this)

fun String.startsWithAny(vararg values: String): Boolean = values.any { startsWith(it) }