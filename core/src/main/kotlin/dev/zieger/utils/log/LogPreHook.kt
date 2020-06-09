package dev.zieger.utils.log


interface ILogPreHook {
    val onPreHook: ILogMessageContext.(message: String) -> Unit
}

object EmptyLogPreHook : ILogPreHook {
    override val onPreHook: ILogMessageContext.(String) -> Unit = {}
}