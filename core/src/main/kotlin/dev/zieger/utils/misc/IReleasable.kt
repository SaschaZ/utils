package dev.zieger.utils.misc

/**
 * Instances implementing this interface need to be released when they are not
 * needed anymore.
 */
interface IReleasable {
    fun release()
}