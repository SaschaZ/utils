package dev.zieger.utils.misc

interface IReleasable {

    /**
     * Call this than this class is not needed anymore.
     */
    fun release()
}