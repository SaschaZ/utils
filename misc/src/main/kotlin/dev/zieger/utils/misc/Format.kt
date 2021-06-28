package dev.zieger.utils.misc

fun Number.format(digits: Int): String = "%.${digits}f".format(toDouble())