package dev.zieger.utils.coroutines.channel.pipeline

data class LogContext(override var id: String) :
    ILogContext {

    override fun v(msg: String) = dev.zieger.utils.log.Log.v("$id -> $msg")
    override fun d(msg: String) = dev.zieger.utils.log.Log.d("$id -> $msg")
    override fun i(msg: String) = dev.zieger.utils.log.Log.i("$id -> $msg")
    override fun w(msg: String) = dev.zieger.utils.log.Log.w("$id -> $msg")
    override fun e(msg: String) = dev.zieger.utils.log.Log.e("$id -> $msg")
}

interface ILogContext : Identity {

    fun v(msg: String)
    fun d(msg: String)
    fun i(msg: String)
    fun w(msg: String)
    fun e(msg: String)
}

val <T : Identity> T.Log: ILogContext
    get() = LogContext(id)