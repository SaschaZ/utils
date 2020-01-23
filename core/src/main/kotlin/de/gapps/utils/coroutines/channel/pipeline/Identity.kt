@file:Suppress("MemberVisibilityCanBePrivate")

package de.gapps.utils.coroutines.channel.pipeline

interface Identity {

    var id: String
}

object NoId : Identity {

    const val NO_ID = "NO_ID"
    override var id: String = NO_ID
}

data class Id(override var id: String) : Identity

interface ILogContext : Identity {

    fun v(msg: String)
    fun d(msg: String)
    fun i(msg: String)
    fun w(msg: String)
    fun e(msg: String)
}

val <T : Identity> T.Log: ILogContext
    get() = LogContext(id)