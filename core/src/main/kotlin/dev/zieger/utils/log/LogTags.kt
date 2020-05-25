package dev.zieger.utils.log

import dev.zieger.utils.misc.asUnit

interface ILogTags {
    val tags: Set<String>

    fun String.addTag()
}

open class LogTags(override val tags: MutableSet<String> = mutableSetOf()) : ILogTags {
    override fun String.addTag() = tags.add(this).asUnit()
}