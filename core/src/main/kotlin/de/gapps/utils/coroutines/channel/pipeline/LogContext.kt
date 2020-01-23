package de.gapps.utils.coroutines.channel.pipeline

data class LogContext(override var id: String) :
    ILogContext {

    override fun v(msg: String) = de.gapps.utils.log.Log.v("$id -> $msg")
    override fun d(msg: String) = de.gapps.utils.log.Log.d("$id -> $msg")
    override fun i(msg: String) = de.gapps.utils.log.Log.i("$id -> $msg")
    override fun w(msg: String) = de.gapps.utils.log.Log.w("$id -> $msg")
    override fun e(msg: String) = de.gapps.utils.log.Log.e("$id -> $msg")
}