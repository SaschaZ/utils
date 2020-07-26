@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.koin.global

import org.koin.core.KoinApplication
import org.koin.core.KoinComponent

interface GKoinComponent : KoinComponent {

    val key: String
    var kApp: KoinApplication

    override fun getKoin() = kApp.koin
}

internal open class GKoinComponentImpl(override val key: String = DEFAULT_GKOIN_KEY) : GKoinComponent {

    companion object {
        const val DEFAULT_GKOIN_KEY = "DEFAULT_GKOIN_KEY"
    }

    override lateinit var kApp: KoinApplication
}

