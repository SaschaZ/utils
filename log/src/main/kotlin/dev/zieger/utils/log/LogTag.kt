package dev.zieger.utils.log

interface ILogTag {

    /**
     * Primary TAG
     */
    var tag: Any?

    fun copyTags(): ILogTag
}

open class LogTag(
    override var tag: Any? = null
) : ILogTag {

    override fun copyTags(): ILogTag = LogTag(tag)
}

fun ILogContext.filterTags(vararg tags: Any) = addPostFilter { next ->
    val t = messageTag ?: tag
    if (t == null || t in tags) next()
    else cancel()
}