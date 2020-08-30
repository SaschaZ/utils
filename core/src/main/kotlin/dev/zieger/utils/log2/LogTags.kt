package dev.zieger.utils.log2

import dev.zieger.utils.misc.asUnit

interface ILogTags {

    val tags: Set<Any>
    var tag: Any?

    operator fun plusAssign(tag: Any)
    operator fun minusAssign(tag: Any)

    fun copyTags(): ILogTags
}

open class LogTags(
    override var tag: Any? = null,
    override val tags: MutableSet<Any> = mutableSetOf()
) : ILogTags {

    override fun plusAssign(tag: Any) = tags.add(tag).asUnit()
    override fun minusAssign(tag: Any) = tags.remove(tag).asUnit()

    override fun copyTags(): ILogTags = LogTags(tag, mutableSetOf(*tags.toTypedArray()))
}