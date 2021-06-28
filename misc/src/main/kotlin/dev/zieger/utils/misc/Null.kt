package dev.zieger.utils.misc

inline fun <T> T.nullWhen(block: (T) -> Boolean): T? = if (block(this)) null else this