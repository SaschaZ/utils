package dev.zieger.utils.coroutines

/**
 * Allows to suspend until [resume] gets called.
 */
open class Continuation : TypeContinuation<Boolean>() {

    /**
     * Resumes all callers of [suspend] until initialization or the last call to [resume].
     */
    open fun resume() = resume(true)
}