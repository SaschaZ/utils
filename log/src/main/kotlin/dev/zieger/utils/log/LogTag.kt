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