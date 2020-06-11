@file:Suppress("MemberVisibilityCanBePrivate")

package dev.zieger.utils.coroutines.channel.pipeline

interface Identity {

    var id: String
}

object NoId : Identity {

    const val NO_ID = "NO_ID"
    override var id: String = NO_ID
}

data class Id(override var id: String) : Identity