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

data class LogContext(override var id: String) : ILogContext {

    override fun v(msg: String) = de.gapps.utils.log.Log.v("$id -> $msg")
    override fun d(msg: String) = de.gapps.utils.log.Log.d("$id -> $msg")
    override fun i(msg: String) = de.gapps.utils.log.Log.i("$id -> $msg")
    override fun w(msg: String) = de.gapps.utils.log.Log.w("$id -> $msg")
    override fun e(msg: String) = de.gapps.utils.log.Log.e("$id -> $msg")
}

val <T : Identity> T.Log: ILogContext
    get() = LogContext(id)