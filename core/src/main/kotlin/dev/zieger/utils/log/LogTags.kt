package dev.zieger.utils.log

import dev.zieger.utils.misc.asUnit

interface ILogTags {
    var tags: MutableSet<String>

    fun String.addTag()
}

open class LogTags(override var tags: MutableSet<String> = mutableSetOf()) : ILogTags {
    override fun String.addTag() = tags.add(this).asUnit()
}