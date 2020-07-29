@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.log

/**
 *      new message
 *      -> apply pre hooks (logLevel filter, spam filter) | can change message content, delay message or remove message from pipeline
 *      -> add meta data
 *      -> apply post hooks (colored output by logLevel, logCache) | can change resulting message
 *      -> write message to output
 */
open class LogPipeline {

    val preHooks: MutableList<ILogMessageContext.() -> String> = ArrayList()
    val postHooks: MutableList<ILogMessageContext.() -> String> = ArrayList()

    fun ILogMessageContext.process(
        midAction: ILogMessageContext.() -> String,
        endAction: ILogMessageContext.() -> Unit
    ) {
        processHooks(preHooks, midAction)
        processHooks(postHooks) { endAction(); "" }
    }

    private fun ILogMessageContext.processHooks(
        hooks: List<ILogMessageContext.() -> String>,
        action: ILogMessageContext.() -> String
    ): String {
        val lambdas = ArrayList<ILogMessageContext.() -> String>()
        var idx = 0
        for (hook in ArrayList(hooks).reversed()) {
            val lambda = when (idx++) {
                0 -> action
                else -> lambdas[idx - 2]
            }
            lambdas.add {
                hook.run { lambda() }
            }
        }
        return (lambdas.lastOrNull() ?: action).invoke(this)
    }
}