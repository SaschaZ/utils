package dev.zieger.utils.log


interface ILogPreHook {
    val onPreHook: ILogMessageContext.(wrappedMessage: String) -> Unit
}

object EmptyLogPreHook : ILogPreHook {
    override val onPreHook: ILogMessageContext.(String) -> Unit = {}
}