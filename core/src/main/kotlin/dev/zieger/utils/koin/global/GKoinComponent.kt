@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package dev.zieger.utils.koin.global

import org.koin.core.KoinApplication
import org.koin.core.KoinComponent
import org.koin.dsl.KoinAppDeclaration

interface GKoinComponent : KoinComponent {

    val key: String
    val kApp: KoinApplication

    override fun getKoin() = kApp.koin

    fun startKoin(appDeclaration: KoinAppDeclaration): KoinApplication
    fun stopKoin()
}

internal open class GKoinComponentImpl(override val key: String = DEFAULT_GKOIN_KEY) : GKoinComponent {

    companion object {
        const val DEFAULT_GKOIN_KEY = "DEFAULT_GKOIN_KEY"
    }

    override lateinit var kApp: KoinApplication

    override fun startKoin(appDeclaration: KoinAppDeclaration) =
        org.koin.core.context.startKoin(appDeclaration).also { kApp = it }

    override fun stopKoin() = kApp.koin.close()
}

